package com.example.a5120_shademate.ui.viewmodel

import com.example.a5120_shademate.data.api.WeatherManager

import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer

class SuburbMapViewModel(weatherManager: WeatherManager) : BaseMapViewModel(weatherManager) {
    override val layerId: String = "sal-2021-aust-gda2020-shp-ad1f0o"
    override val isLga: Boolean = false
    private val lineLayerId = "suburb-line-layer"

    override fun setupLayer(style: com.mapbox.maps.Style) {
        val fill = style.getLayer(layerId) as? FillLayer
        fill?.let { layer ->
            layer.fillColor(getColorExpression())
            layer.fillOpacity(Expression.step(Expression.zoom(), Expression.literal(0.0), Expression.literal(10.0), Expression.literal(0.5)))

            if (style.getLayer(lineLayerId) == null) {
                val line = LineLayer(lineLayerId, layer.sourceId).sourceLayer(layer.sourceLayer ?: "")
                line.lineColor(android.graphics.Color.BLACK).lineWidth(0.8)
                line.lineOpacity(Expression.step(Expression.zoom(), Expression.literal(0.0), Expression.literal(10.0), Expression.literal(0.4)))
                style.addLayer(line)
            }
        }
    }

    override fun getFeatureId(feature: com.mapbox.geojson.Feature): String? {
        return feature.id() ?: feature.getProperty("SAL_CODE21")?.asString
    }
}
