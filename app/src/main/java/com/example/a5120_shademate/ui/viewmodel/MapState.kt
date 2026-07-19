package com.example.a5120_shademate.ui.viewmodel

import com.mapbox.geojson.Point

data class MapState(
    val currentZoom: Float = 10f,
    val isLgaMode: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val suburbTemperatureCache: Map<String, Double> = emptyMap(),
    val lgaTemperatureCache: Map<String, Double> = emptyMap(),
    val heatMapUrl: String? = null,
    val heatMapBbox: List<Double>? = null,
    val heatMapSuburb: String? = null,
    val isHeatMapOverlayActive: Boolean = false
)
