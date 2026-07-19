package com.example.a5120_shademate.model

data class HeatZone(
    val id: String,
    val name: String,
    val temperatureCelsius: Int,
    val level: HeatLevel,
    val xRatio: Float,
    val yRatio: Float,
    val summary: String,
)

data class HeatMapData(
    val cityName: String,
    val lastUpdated: String,
    val zones: List<HeatZone>,
)

data class HeatFact(
    val id: String,
    val title: String,
    val description: String,
)

data class HeatStatistic(
    val id: String,
    val title: String,
    val value: String,
    val description: String,
)

data class HeatEducationData(
    val facts: List<HeatFact>,
    val statistics: List<HeatStatistic>,
)
