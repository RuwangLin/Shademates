package com.example.a5120_shademate.model

import com.google.gson.annotations.SerializedName

/**
 * OpenWeather API response models
 */
data class WeatherResponse(
    @SerializedName("main") val main: MainWeatherData,
    @SerializedName("name") val cityName: String,
    @SerializedName("id") val cityId: Int
)

data class MainWeatherData(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int
)
