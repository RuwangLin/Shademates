package com.example.a5120_shademate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suburb_weather")
data class SuburbWeather(
    @PrimaryKey val id: String, // suburb name
    val minTemp: Int,
    val maxTemp: Int,
    val midTemp: Int,
    val uvi: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
