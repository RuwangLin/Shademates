package com.example.a5120_shademate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Query("SELECT * FROM suburb_weather WHERE id = :id LIMIT 1")
    suspend fun getWeatherById(id: String): SuburbWeather?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: SuburbWeather)

    @Query("DELETE FROM suburb_weather WHERE timestamp < :expirationTime")
    suspend fun deleteOldWeather(expirationTime: Long)
}
