package com.example.a5120_shademate.data.api

import com.example.a5120_shademate.BuildConfig
import android.util.Log
import com.example.a5120_shademate.data.local.SuburbWeather
import com.example.a5120_shademate.data.local.WeatherDao
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class CoolPlaceTemperatureResolver(
    private val weatherDao: WeatherDao,
    private val weatherService: WeatherApiService = WeatherApiClient.retrofit.create(WeatherApiService::class.java),
) {
    private val apiKey = BuildConfig.OPENWEATHER_API_KEY
    private val gridCache = ConcurrentHashMap<String, Double>()

    suspend fun enrich(places: List<CoolPlaceCardData>): List<CoolPlaceCardData> = withContext(Dispatchers.IO) {
        if (places.isEmpty()) return@withContext emptyList()

        val unresolvedPlaces = places.filter { it.temperatureCelsius == null }
        if (unresolvedPlaces.isEmpty()) return@withContext places

        val todayStart = getTodayStartTimestamp()
        val distinctGrids = unresolvedPlaces
            .map { place ->
                val latIdx = (place.latitude / SUBURB_LAT_STEP).roundToInt()
                val lonIdx = (place.longitude / SUBURB_LON_STEP).roundToInt()
                val gridId = "${GRID_PREFIX}_${latIdx}_${lonIdx}"
                val coords = latIdx * SUBURB_LAT_STEP to lonIdx * SUBURB_LON_STEP
                gridId to coords
            }
            .distinctBy { it.first }

        distinctGrids.forEach { (gridId, coords) ->
            if (gridCache.containsKey(gridId)) return@forEach

            val cached = weatherDao.getWeatherById(gridId)
            if (cached != null && cached.timestamp >= todayStart) {
                gridCache[gridId] = cached.midTemp.toDouble()
                return@forEach
            }

            val resolvedTemp = runCatching {
                val response = weatherService.getOneCallWeather(coords.first, coords.second, apiKey = apiKey)
                val daily = response.daily?.firstOrNull()
                val min = daily?.temp?.min?.toInt() ?: 18
                val max = daily?.temp?.max?.toInt() ?: 30
                val mid = (min + max) / 2
                val uvi = response.current.uvi ?: 0.0
                weatherDao.insertWeather(
                    SuburbWeather(
                        id = gridId,
                        minTemp = min,
                        maxTemp = max,
                        midTemp = mid,
                        uvi = uvi,
                    )
                )
                mid.toDouble()
            }.onFailure { throwable ->
                Log.e("CoolPlaceTempResolver", "Failed to resolve grid $gridId: ${throwable.message}")
            }.getOrNull()

            if (resolvedTemp != null) {
                gridCache[gridId] = resolvedTemp
            }
        }

        places.map { place ->
            if (place.temperatureCelsius != null) {
                place
            } else {
                val latIdx = (place.latitude / SUBURB_LAT_STEP).roundToInt()
                val lonIdx = (place.longitude / SUBURB_LON_STEP).roundToInt()
                val gridId = "${GRID_PREFIX}_${latIdx}_${lonIdx}"
                val baseTemp = gridCache[gridId]
                val estimatedTemp = baseTemp?.plus(getUhiFactor(place.suburb))?.roundToInt()
                place.copy(temperatureCelsius = estimatedTemp)
            }
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

    private fun getUhiFactor(suburbName: String): Double {
        val name = suburbName.lowercase()
        return when {
            name.contains("melbourne") -> 3.2
            name.contains("docklands") || name.contains("southbank") -> 2.1
            name.contains("carlton") || name.contains("parkland") -> -0.8
            else -> (suburbName.hashCode() % 10) / 5.0 - 1.0
        }
    }

    private companion object {
        const val GRID_PREFIX = "cool_place_grid"
        const val SUBURB_LAT_STEP = 0.045
        const val SUBURB_LON_STEP = 0.057
    }
}
