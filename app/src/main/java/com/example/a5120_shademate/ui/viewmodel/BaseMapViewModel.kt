package com.example.a5120_shademate.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.a5120_shademate.data.api.WeatherManager
import com.mapbox.maps.QueriedRenderedFeature
import com.mapbox.geojson.Feature
import com.mapbox.geojson.MultiPolygon
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseMapViewModel(
    protected val weatherManager: WeatherManager
) : ViewModel() {

    protected val _temperatureCache = MutableStateFlow<Map<String, Double>>(emptyMap())
    val temperatureCache: StateFlow<Map<String, Double>> = _temperatureCache.asStateFlow()

    // Keep a stable ID-to-name map for list labels and UHI lookups.
    protected val _featureNames = mutableMapOf<String, String>()

    abstract val layerId: String
    abstract val isLga: Boolean

    // Each concrete map view model decides how its layer should be initialized.
    abstract fun setupLayer(style: com.mapbox.maps.Style)

    // Build the live color expression from the latest cached temperatures.
    fun getColorExpression(): com.mapbox.maps.extension.style.expressions.generated.Expression {
        val temps = _temperatureCache.value.values
        val min = temps.minOrNull() ?: 15.0
        val max = temps.maxOrNull() ?: 35.0
        val range = (max - min).coerceAtLeast(1.0)

        return com.mapbox.maps.extension.style.expressions.generated.Expression.interpolate(
            com.mapbox.maps.extension.style.expressions.generated.Expression.linear(),
            com.mapbox.maps.extension.style.expressions.generated.Expression.coalesce(
                com.mapbox.maps.extension.style.expressions.generated.Expression.featureState(
                    com.mapbox.maps.extension.style.expressions.generated.Expression.literal("temp")
                ),
                com.mapbox.maps.extension.style.expressions.generated.Expression.literal(-1.0)
            ),
            com.mapbox.maps.extension.style.expressions.generated.Expression.literal(-1.0), com.mapbox.maps.extension.style.expressions.generated.Expression.color(android.graphics.Color.TRANSPARENT),
            com.mapbox.maps.extension.style.expressions.generated.Expression.literal(min), com.mapbox.maps.extension.style.expressions.generated.Expression.color(android.graphics.Color.BLUE),
            com.mapbox.maps.extension.style.expressions.generated.Expression.literal(min + range * 0.5), com.mapbox.maps.extension.style.expressions.generated.Expression.color(android.graphics.Color.YELLOW),
            com.mapbox.maps.extension.style.expressions.generated.Expression.literal(max), com.mapbox.maps.extension.style.expressions.generated.Expression.color(android.graphics.Color.RED)
        )
    }

    abstract fun getFeatureId(feature: Feature): String?

    fun getTopHotZones(limit: Int = 3): List<com.example.a5120_shademate.model.HeatZone> {
        return _temperatureCache.value.entries
            .filter { it.value > 0 } // Ignore placeholder values
            .sortedByDescending { it.value }
            .take(limit)
            .map { (id, temp) ->
                val name = _featureNames[id] ?: id
                val level = when {
                    temp >= 35 -> com.example.a5120_shademate.model.HeatLevel.EXTREME
                    temp >= 31 -> com.example.a5120_shademate.model.HeatLevel.HOT
                    temp >= 26 -> com.example.a5120_shademate.model.HeatLevel.WARM
                    else -> com.example.a5120_shademate.model.HeatLevel.COOL
                }
                com.example.a5120_shademate.model.HeatZone(
                    id = id,
                    name = name,
                    temperatureCelsius = temp.toInt(),
                    level = level,
                    xRatio = 0f, // Not used in list
                    yRatio = 0f, // Not used in list
                    summary = "Live map data"
                )
            }
    }

    fun extractDataFromFeatures(features: List<QueriedRenderedFeature>): Map<String, Triple<Double, Double, String>> {
        val dataToUpdate = mutableMapOf<String, Triple<Double, Double, String>>()

        features.forEach { renderedFeature ->
            if (renderedFeature.layers.contains(layerId)) {
                val f = renderedFeature.queriedFeature.feature
                // Keep this debug log while validating feature IDs and layer queries.
                Log.d("MapboxDebug", "Layer: $layerId, Feature Root ID: ${f.id()}, Properties: ${f.properties()}")

                val id = getFeatureId(f)

                if (id != null) {
                    // Cache property-based name for display/UHI if root ID is numeric
                    val propertyName = f.getProperty(if (isLga) "LGA_NAME25" else "SAL_NAME21")?.asString ?: id
                    _featureNames[id] = propertyName

                    val geom = f.geometry()
                    val coords = when (geom) {
                        is Point -> Pair(geom.latitude(), geom.longitude())
                        is Polygon -> {
                            val ring = geom.coordinates()[0]
                            if (ring.isNotEmpty()) {
                                Pair(ring.map { it.latitude() }.average(), ring.map { it.longitude() }.average())
                            } else null
                        }
                        is MultiPolygon -> {
                            val ring = geom.coordinates()[0][0]
                            if (ring.isNotEmpty()) {
                                Pair(ring.map { it.latitude() }.average(), ring.map { it.longitude() }.average())
                            } else null
                        }
                        else -> null
                    }
                    if (coords != null) dataToUpdate[id] = Triple(coords.first, coords.second, propertyName)
                }
            }
        }
        return dataToUpdate
    }

    fun updateWeatherData(
        sourceId: String,
        sourceLayerId: String,
        dataToUpdate: Map<String, Triple<Double, Double, String>>,
        mapboxMap: com.mapbox.maps.MapboxMap,
        onComplete: () -> Unit
    ) {
        weatherManager.updateTemperature(
            mapboxMap = mapboxMap,
            sourceId = sourceId,
            sourceLayerId = sourceLayerId,
            features = dataToUpdate,
            isLga = isLga
        ) {
            // Update local cache after manager finishes
            _temperatureCache.value = weatherManager.temperatureCache.toMap()
            onComplete()
        }
    }
}
