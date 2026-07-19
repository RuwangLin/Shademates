package com.example.a5120_shademate.model

data class HomeAreaTemperature(
    val areaName: String,
    val lowCelsius: Int?,
    val midCelsius: Int?,
    val highCelsius: Int?,
)

data class CurrentLocationWeather(
    val areaName: String,
    val currentCelsius: Int?,
    val lowCelsius: Int?,
    val highCelsius: Int?,
    val feelsLikeCelsius: Int? = null,
    val shadeCoverageScore: Int? = null,
    val heatExposureScore: Int? = null,
)

data class HomeHeatOverview(
    val uvIndex: Int?,
    val uvLabel: String,
    val areaTemperatures: List<HomeAreaTemperature>,
    val quickTips: List<String>,
    val currentLocationWeather: CurrentLocationWeather? = null,
)
