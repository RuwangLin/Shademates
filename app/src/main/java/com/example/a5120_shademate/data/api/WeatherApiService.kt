package com.example.a5120_shademate.data.api

import com.example.a5120_shademate.model.WeatherResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeatherMap API service definition
 */
interface WeatherApiService {

    @GET("weather")
    suspend fun getWeatherData(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Celsius
    ): WeatherResponse

    @GET("weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    /**
     * OpenWeather One Call API 3.0
     */
    @GET("onecall")
    suspend fun getOneCallWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely,hourly,alerts",
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OneCallWeatherResponse
}

data class OneCallWeatherResponse(
    @SerializedName("current") val current: CurrentWeather,
    @SerializedName("daily") val daily: List<DailyWeather>?
)

data class CurrentWeather(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double? = null,
    @SerializedName("uvi") val uvi: Double? = null
)

data class DailyWeather(
    @SerializedName("temp") val temp: DailyTemp
)

data class DailyTemp(
    @SerializedName("day") val day: Double,
    @SerializedName("min") val min: Double,
    @SerializedName("max") val max: Double
)
