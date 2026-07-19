package com.example.a5120_shademate.data.api

import com.example.a5120_shademate.BuildConfig
import android.util.Log
import com.example.a5120_shademate.data.local.SuburbWeather
import com.example.a5120_shademate.data.local.WeatherDao
import com.mapbox.bindgen.Value
import com.mapbox.maps.MapboxMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

class WeatherManager(
    private val weatherDao: WeatherDao
) {
    private val apiKey = BuildConfig.OPENWEATHER_API_KEY

    // Grid sizes are finer for suburb mode and coarser for LGA mode.
    private val SUBURB_LAT_STEP = 0.045 // ~5km
    private val SUBURB_LON_STEP = 0.057
    private val LGA_LAT_STEP = 0.225    // ~25km
    private val LGA_LON_STEP = 0.285

    private val gridCache = ConcurrentHashMap<String, Double>()
    private val fetchingGrids = ConcurrentHashMap.newKeySet<String>()
    val temperatureCache = ConcurrentHashMap<String, Double>()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/3.0/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(WeatherApiService::class.java)

    /**
     * Shared temperature refresh logic for both suburb and LGA overlays.
     * `isLga` switches the grid size and whether UHI adjustments should be applied.
     */
    fun updateTemperature(
        mapboxMap: MapboxMap,
        sourceId: String,
        sourceLayerId: String?,
        features: Map<String, Triple<Double, Double, String>>,
        isLga: Boolean,
        onUpdate: () -> Unit = {}
    ) {
        val latStep = if (isLga) LGA_LAT_STEP else SUBURB_LAT_STEP
        val lonStep = if (isLga) LGA_LON_STEP else SUBURB_LON_STEP
        val prefix = if (isLga) "lga_grid" else "sub_grid"

        CoroutineScope(Dispatchers.IO).launch {
            val todayStart = getTodayStartTimestamp()

            // 1. Work out which weather grids are needed for the visible map features.
            val requiredGrids = features.values.map {
                val latIdx = (it.first / latStep).roundToInt()
                val lonIdx = (it.second / lonStep).roundToInt()
                "${prefix}_${latIdx}_${lonIdx}" to Pair(latIdx * latStep, lonIdx * lonStep)
            }.distinctBy { it.first }

            // 2. Only request grids that are not already cached today, capped to nine API calls.
            val gridsToFetch = requiredGrids.filter { (gridId, _) ->
                if (gridCache.containsKey(gridId)) return@filter false
                val dbEntry = weatherDao.getWeatherById(gridId)
                if (dbEntry != null && dbEntry.timestamp >= todayStart) {
                    gridCache[gridId] = dbEntry.midTemp.toDouble()
                    return@filter false
                }
                true
            }.take(9)

            // 3. Fetch the missing grid temperatures from the weather API.
            gridsToFetch.forEach { (gridId, coords) ->
                if (fetchingGrids.add(gridId)) {
                    try {
                        kotlinx.coroutines.delay(600)
                        val response = service.getOneCallWeather(coords.first, coords.second, apiKey = apiKey)
                        val daily = response.daily?.firstOrNull()
                        val min = daily?.temp?.min?.toInt() ?: 18
                        val max = daily?.temp?.max?.toInt() ?: 30
                        val mid = (min + max) / 2
                        val uvi = response.current?.uvi ?: 0.0


                        weatherDao.insertWeather(SuburbWeather(gridId, min, max, mid, uvi,System.currentTimeMillis()))
                        gridCache[gridId] = mid.toDouble()
                    } catch (e: Exception) {
                        Log.e("WeatherManager", "Error: ${e.message}")
                    } finally {
                        fetchingGrids.remove(gridId)
                    }
                }
            }

            // 4. Apply the resolved temperatures back onto visible map features.
            val availableGrids = gridCache.toList()
            features.forEach { (id, data) ->
                val coords = Pair(data.first, data.second)
                val propertyName = data.third
                val baseTemp = findNearestGridTemp(coords, availableGrids, latStep, lonStep) ?: 18.0
                // Keep UHI adjustment for suburb mode, but skip it for the broader LGA layer.
                val finalTemp = if (isLga) baseTemp else (baseTemp + getUhiFactor(propertyName))

                temperatureCache[id] = finalTemp
                applyTemperatureToMap(mapboxMap, sourceId, sourceLayerId, id, finalTemp)
            }

            CoroutineScope(Dispatchers.Main).launch { onUpdate() }
        }
    }

    private fun findNearestGridTemp(target: Pair<Double, Double>, available: List<Pair<String, Double>>, latStep: Double, lonStep: Double): Double? {
        if (available.isEmpty()) return null
        return available.minByOrNull { (gridId, _) ->
            val parts = gridId.split("_")
            if (parts.size < 3) return@minByOrNull Double.MAX_VALUE
            val gLat = parts[parts.size - 2].toDouble() * latStep
            val gLon = parts[parts.size - 1].toDouble() * lonStep
            val dx = target.first - gLat
            val dy = target.second - gLon
            dx * dx + dy * dy
        }?.second
    }

    private fun getUhiFactor(suburbId: String): Double {
        val name = suburbId.lowercase()
        return when {
            name.contains("melbourne") || name.contains("21640") -> 3.2
            name.contains("docklands") || name.contains("southbank") -> 2.1
            name.contains("carlton") || name.contains("parkland") -> -0.8
            else -> (suburbId.hashCode() % 10) / 5.0 - 1.0
        }
    }

    private fun getTodayStartTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun applyTemperatureToMap(mapboxMap: MapboxMap, sourceId: String, sourceLayerId: String?, featureId: String, temp: Double) {
        val stateMap = HashMap<String, Value>()
        stateMap["temp"] = Value(temp)
        val stateValue = Value(stateMap)
        CoroutineScope(Dispatchers.Main).launch {
            mapboxMap.setFeatureState(sourceId, sourceLayerId, featureId, stateValue) { _ -> }
        }
    }
}
