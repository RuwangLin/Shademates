package com.example.a5120_shademate.data.api

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement
import com.google.gson.JsonParser

data class CoolPlacesResponse(
    val status: String? = null,
    val data: CoolPlacesData? = null
)

data class CoolPlacesData(
    val items: List<CoolPlaceDto>? = null,
    val count: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null
)

data class CoolPlaceDto(
    @SerializedName("coolplace_id") val id: Int? = null,
    @SerializedName("location_id") val locationId: Int? = null,
    @SerializedName("source_id") val sourceId: Int? = null,
    @SerializedName("place_name") val name: String? = null,
    @SerializedName("place_type") val type: String? = null,
    val theme: String? = null,
    @SerializedName("sub_theme") val subTheme: String? = null,
    @SerializedName("opening_hours") val openingHours: String? = null,
    @SerializedName("shade_level") val shadeLevel: String? = null,
    @SerializedName("cooling_score") val coolingScore: Double? = null,
    @SerializedName("has_water_access") val hasWaterAccess: Int? = null,
    @SerializedName("has_seating") val hasSeating: Int? = null,
    @SerializedName("is_accessible") val isAccessible: Int? = null,
    @SerializedName("is_indoor") val isIndoor: Int? = null,
    @SerializedName("location_name") val locationName: String? = null,
    @SerializedName("location_address") val locationAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("distance_val") val distanceVal: Double? = null,
    @SerializedName("display_distance") val displayDistance: String? = null,
    val suburb: String? = null,

    // Some backend payloads return JSON arrays as string values (for example "[\"park\"]").
    // Keep these fields flexible and normalize them in the mapper layer.
    @SerializedName("filters") val filters: JsonElement? = null,
    val categories: JsonElement? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

internal fun JsonElement?.toStringListOrEmpty(): List<String> {
    if (this == null || isJsonNull) return emptyList()

    return when {
        isJsonArray -> asJsonArray.mapNotNull { element ->
            element.takeIf { it.isJsonPrimitive }?.asString?.trim()?.takeIf { it.isNotBlank() }
        }

        isJsonPrimitive && asJsonPrimitive.isString -> {
            val rawValue = asString.trim()
            if (rawValue.isBlank()) {
                emptyList()
            } else if (rawValue.startsWith("[") && rawValue.endsWith("]")) {
                runCatching {
                    JsonParser.parseString(rawValue).asJsonArray.mapNotNull { element ->
                        element.takeIf { it.isJsonPrimitive }?.asString?.trim()?.takeIf { it.isNotBlank() }
                    }
                }.getOrDefault(emptyList())
            } else {
                listOf(rawValue)
            }
        }

        else -> emptyList()
    }
}
