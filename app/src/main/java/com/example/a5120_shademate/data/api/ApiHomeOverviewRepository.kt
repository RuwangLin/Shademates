package com.example.a5120_shademate.data.api

import com.example.a5120_shademate.BuildConfig
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.a5120_shademate.data.HomeOverviewRepository
import com.example.a5120_shademate.data.UserProfilePreferences
import com.example.a5120_shademate.data.local.SuburbWeather
import com.example.a5120_shademate.data.local.WeatherDao
import com.example.a5120_shademate.model.CurrentLocationWeather
import com.example.a5120_shademate.model.HomeAreaTemperature
import com.example.a5120_shademate.model.HomeHeatOverview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class ApiHomeOverviewRepository(
    private val context: Context,
    private val weatherDao: WeatherDao,
    private val weatherService: WeatherApiService = WeatherApiClient.retrofit.create(WeatherApiService::class.java),
    private val uvTipsService: UvTipsApiService = AppApiClient.retrofit.create(UvTipsApiService::class.java),
    private val heatMapService: HeatMapApiService = AppApiClient.retrofit.create(HeatMapApiService::class.java),
) : HomeOverviewRepository {

    private val apiKey = BuildConfig.OPENWEATHER_API_KEY
    private val uvTipsApiKey = BuildConfig.UV_TIPS_API_KEY
    private val cacheExpirationMs = 24 * 60 * 60 * 1000L

    private val suburbs = listOf(
        SuburbCoords("Melbourne CBD", -37.8136, 144.9631),
        SuburbCoords("Footscray", -37.7997, 144.8993),
        SuburbCoords("Brunswick", -37.7670, 144.9590),
        SuburbCoords("St Kilda", -37.8675, 144.9764),
        SuburbCoords("Dandenong", -37.9810, 145.2150),
        SuburbCoords("Richmond", -37.8230, 144.9980),
        SuburbCoords("Carlton", -37.8030, 144.9670),
        SuburbCoords("Docklands", -37.8170, 144.9460),
        SuburbCoords("Southbank", -37.8228, 144.9620),
        SuburbCoords("South Yarra", -37.8400, 144.9890),
        SuburbCoords("Fitzroy", -37.8000, 144.9780),
        SuburbCoords("North Melbourne", -37.8000, 144.9450),
    )

    override suspend fun getHomeHeatOverview(lat: Double?, lon: Double?): HomeHeatOverview = withContext(Dispatchers.IO) {
        Log.d("ApiHomeOverview", "Requesting home heat overview")

        val baseOverview = getFallbackBaseOverview()

        val targetLat = lat ?: -37.8136
        val targetLon = lon ?: 144.9631
        val nearestSuburbs = suburbs
            .sortedBy { calculateDistance(targetLat, targetLon, it.lat, it.lon) }
            .take(3)

        val currentSnapshot = fetchCurrentLocationSnapshot(targetLat, targetLon)
        var displayUvi = currentSnapshot?.uvi ?: 0.0

        val finalAreaTemperatures = nearestSuburbs.mapIndexed { index, suburb ->
            val cached = weatherDao.getWeatherById(suburb.name)
            val isExpired = cached != null && (System.currentTimeMillis() - cached.timestamp > cacheExpirationMs)

            if (cached != null && !isExpired) {
                if (index == 0 && currentSnapshot == null) {
                    displayUvi = cached.uvi
                }
                HomeAreaTemperature(cached.id, cached.minTemp, cached.midTemp, cached.maxTemp)
            } else {
                val freshWeather = try {
                    weatherService.getOneCallWeather(suburb.lat, suburb.lon, apiKey = apiKey)
                } catch (e: Exception) {
                    Log.e("ApiHomeOverview", "Failed suburb weather fetch for ${suburb.name}: ${e.message}")
                    null
                }

                if (freshWeather != null) {
                    val daily = freshWeather.daily?.firstOrNull()
                    val min = daily?.temp?.min?.toInt() ?: 18
                    val max = daily?.temp?.max?.toInt() ?: 30
                    val mid = ((daily?.temp?.day ?: freshWeather.current.temp)).toInt()
                    val uvi = freshWeather.current.uvi ?: 0.0

                    if (index == 0 && currentSnapshot == null) {
                        displayUvi = uvi
                    }

                    weatherDao.insertWeather(
                        SuburbWeather(
                            id = suburb.name,
                            minTemp = min,
                            maxTemp = max,
                            midTemp = mid,
                            uvi = uvi,
                        )
                    )
                    HomeAreaTemperature(suburb.name, min, mid, max)
                } else {
                    cached?.let {
                        if (index == 0 && currentSnapshot == null) {
                            displayUvi = it.uvi
                        }
                        HomeAreaTemperature(it.id, it.minTemp, it.midTemp, it.maxTemp)
                    } ?: HomeAreaTemperature(suburb.name, null, null, null)
                }
            }
        }

        val tipLocation = formatCoordinates(targetLat, targetLon)
        val tipTemperature = currentSnapshot?.weather?.currentCelsius
            ?: finalAreaTemperatures.firstOrNull { it.midCelsius != null }?.midCelsius
        val personalizedUvTip = fetchPersonalizedUvTip(
            location = tipLocation,
            temperatureCelsius = tipTemperature,
            uvIndex = displayUvi,
        )

        baseOverview.copy(
            uvIndex = displayUvi.toInt(),
            uvLabel = getUvLabel(displayUvi),
            areaTemperatures = finalAreaTemperatures,
            quickTips = mergeQuickTips(personalizedUvTip, baseOverview.quickTips),
            currentLocationWeather = currentSnapshot?.weather
                ?: buildFallbackCurrentLocationWeather(
                    lat = targetLat,
                    lon = targetLon,
                    areaTemperatures = finalAreaTemperatures,
                ),
        )
    }

    private suspend fun fetchPersonalizedUvTip(
        location: String,
        temperatureCelsius: Int?,
        uvIndex: Double,
    ): String? {
        val profile = UserProfilePreferences.read(context)
        val request = UvTipsRequest(
            location = location,
            currentTime = buildCurrentTimeLabel(),
            temperature = (temperatureCelsius ?: 0).toString(),
            uvIndex = uvIndex.toInt().toString(),
            ageGroup = profile.ageGroup,
            // The Customise screen now collects a heat-sensitivity choice, but
            // it is still persisted and sent through the existing skinHistory
            // field so the request stays aligned with the current backend API.
            skinHistory = profile.skinHistory,
        )

        return runCatching {
            uvTipsService
                .postUvTips(apiKey = uvTipsApiKey, request = request)
                .extractTip()
        }.onFailure { error ->
            Log.e("ApiHomeOverview", "Failed UV tips fetch: ${error.message}")
        }.getOrNull()
    }

    private suspend fun fetchCurrentLocationSnapshot(lat: Double, lon: Double): CurrentLocationSnapshot? {
        val weather = try {
            weatherService.getOneCallWeather(lat, lon, apiKey = apiKey)
        } catch (e: Exception) {
            Log.e("ApiHomeOverview", "Failed current location weather fetch: ${e.message}")
            return null
        }

        val daily = weather.daily?.firstOrNull()
        val areaName = resolveAreaName(lat, lon) ?: findNearestSuburbName(lat, lon) ?: "Current location"

        // Fetch Shade Coverage from HeatMap API
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        val heatMapData = try {
            heatMapService.getHeatMapAverage(
                HeatMapAverageRequest(
                    lat = lat,
                    lon = lon,
                    currentMonth = month,
                    currentHour = hour
                )
            )
        } catch (e: Exception) {
            Log.e("ApiHomeOverview", "Failed heatmap data fetch: ${e.message}")
            null
        }

        // Convert sun_exposure_ratio to percentage for Shade Coverage Score
        // Shade = 1 - Sun Exposure
        val sunRatio = heatMapData?.sunExposureRatio
        val shadeScore = sunRatio?.let { ((1.0 - it) * 100).toInt() }

        // Calculate Heat Exposure Score based on feels-like temperature and sun exposure
        val heatScore = calculateHeatExposureScore(
            weather.current.feelsLike?.toInt(),
            sunRatio
        )

        return CurrentLocationSnapshot(
            weather = CurrentLocationWeather(
                areaName = areaName,
                currentCelsius = weather.current.temp.toInt(),
                lowCelsius = daily?.temp?.min?.toInt(),
                highCelsius = daily?.temp?.max?.toInt(),
                feelsLikeCelsius = weather.current.feelsLike?.toInt(),
                shadeCoverageScore = shadeScore,
                heatExposureScore = heatScore,
            ),
            uvi = weather.current.uvi ?: 0.0,
        )
    }

    /**
     * Calculates a Heat Exposure Score from 0 to 100.
     * Temp counts for 70%, Sun Exposure for 30%.
     */
    private fun calculateHeatExposureScore(feelsLikeTemp: Int?, sunRatio: Double?): Int? {
        if (feelsLikeTemp == null || sunRatio == null) return null

        // Normalize temperature: Assume 15°C is "low risk" (0) and 40°C is "extreme risk" (100)
        val tempMin = 15.0
        val tempMax = 40.0
        val tempFactor = ((feelsLikeTemp - tempMin) / (tempMax - tempMin)).coerceIn(0.0, 1.0)

        // Combine: 70% Temperature + 30% Sun Exposure
        val combinedScore = (tempFactor * 70.0) + (sunRatio * 30.0)

        return combinedScore.toInt().coerceIn(0, 100)
    }

    private suspend fun resolveAreaName(lat: Double, lon: Double): String? {
        if (!Geocoder.isPresent()) return null

        val geocoder = Geocoder(context, Locale.getDefault())
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val areaName = address?.subLocality
                            ?: address?.locality
                            ?: address?.subAdminArea
                            ?: address?.adminArea
                        continuation.resume(areaName)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lon, 1)
                    ?.firstOrNull()
                    ?.let { address ->
                        address.subLocality
                            ?: address.locality
                            ?: address.subAdminArea
                            ?: address.adminArea
                    }
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun getFallbackBaseOverview() = HomeHeatOverview(
        uvIndex = 0,
        uvLabel = "UNKNOWN",
        areaTemperatures = emptyList(),
        quickTips = listOf("Drink water", "Stay in shade"),
        currentLocationWeather = null,
    )

    private fun mergeQuickTips(personalizedTip: String?, baseTips: List<String>): List<String> {
        return buildList {
            personalizedTip?.takeIf { it.isNotBlank() }?.let(::add)
            addAll(baseTips)
        }.distinct()
    }

    private fun getUvLabel(uv: Double): String {
        return when {
            uv < 3 -> "LOW"
            uv < 6 -> "MODERATE"
            uv < 8 -> "HIGH"
            uv < 11 -> "VERY HIGH"
            else -> "EXTREME"
        }
    }

    private fun buildFallbackCurrentLocationWeather(
        lat: Double,
        lon: Double,
        areaTemperatures: List<HomeAreaTemperature>,
    ): CurrentLocationWeather? {
        val fallbackArea = areaTemperatures.firstOrNull {
            it.lowCelsius != null || it.midCelsius != null || it.highCelsius != null
        } ?: return null

        val areaName = fallbackArea.areaName.ifBlank {
            findNearestSuburbName(lat, lon) ?: "Current location"
        }

        return CurrentLocationWeather(
            areaName = areaName,
            currentCelsius = fallbackArea.midCelsius,
            lowCelsius = fallbackArea.lowCelsius,
            highCelsius = fallbackArea.highCelsius,
        )
    }

    private fun buildCurrentTimeLabel(): String {
        return SimpleDateFormat("h:mm a", Locale.US).format(Date())
    }

    private fun formatCoordinates(lat: Double, lon: Double): String {
        return String.format(Locale.US, "%.5f, %.5f", lat, lon)
    }

    private fun findNearestSuburbName(lat: Double, lon: Double): String? {
        return suburbs.minByOrNull { calculateDistance(lat, lon, it.lat, it.lon) }?.name
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return earthRadiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private data class SuburbCoords(val name: String, val lat: Double, val lon: Double)

    private data class CurrentLocationSnapshot(
        val weather: CurrentLocationWeather,
        val uvi: Double,
    )
}
