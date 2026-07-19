package com.example.a5120_shademate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a5120_shademate.data.api.WeatherManager
import com.example.a5120_shademate.data.local.AppDatabase

class MapViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val weatherManager = WeatherManager(db.weatherDao())

        return when {
            modelClass.isAssignableFrom(SuburbMapViewModel::class.java) ->
                SuburbMapViewModel(weatherManager) as T
            modelClass.isAssignableFrom(LgaMapViewModel::class.java) ->
                LgaMapViewModel(weatherManager) as T
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                val s = SuburbMapViewModel(weatherManager)
                val l = LgaMapViewModel(weatherManager)
                MapViewModel(s, l) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
