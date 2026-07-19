package com.example.a5120_shademate.data.api

import androidx.compose.ui.graphics.Color
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.example.a5120_shademate.ui.components.CoolPlaceCategory

internal fun CoolPlacesResponse.toCoolPlaceCardDataList(): List<CoolPlaceCardData> {
    return data?.items.orEmpty().mapNotNull { it.toUiModel() }
}

private fun CoolPlaceDto.toUiModel(): CoolPlaceCardData? {
    val safeId = id?.toString() ?: return null
    val safeName = name?.takeIf { it.isNotBlank() } ?: return null
    val safeLatitude = latitude ?: return null
    val safeLongitude = longitude ?: return null
    val parsedFilters = filters.toStringListOrEmpty()
    val parsedCategories = categories.toStringListOrEmpty()
    val filterValues = parsedFilters.ifEmpty { parsedCategories }

    val baseCategories = mutableSetOf<CoolPlaceCategory>()
    baseCategories += filterValues.mapNotNull(::toCoolPlaceCategory)

    val lowerType = type?.lowercase()?.trim() ?: ""
    when {
        lowerType.contains("fountain") || lowerType.contains("water") -> {
            baseCategories.add(CoolPlaceCategory.WATER_FOUNTAIN)
            baseCategories.add(CoolPlaceCategory.OUTDOOR)
        }

        lowerType.contains("park") || lowerType.contains("garden") -> {
            baseCategories.add(CoolPlaceCategory.PARK)
            baseCategories.add(CoolPlaceCategory.OUTDOOR)
        }

        lowerType.contains("library") || lowerType.contains("centre") || isIndoor == 1 -> {
            baseCategories.add(CoolPlaceCategory.INDOOR)
            baseCategories.add(CoolPlaceCategory.AC)
        }
    }

    if (baseCategories.isEmpty()) {
        baseCategories.add(CoolPlaceCategory.OTHER)
    }

    val displayTags = parsedCategories.ifEmpty {
        parsedFilters.ifEmpty {
            listOfNotNull(
                type,
                if (hasWaterAccess == 1) "Water Access" else null,
                if (shadeLevel != null) "Shade: $shadeLevel" else null,
            )
        }
    }

    return CoolPlaceCardData(
        id = safeId,
        name = safeName,
        suburb = suburb ?: "Melbourne",
        type = type.orEmpty(),
        distanceMeters = resolveDistanceMeters(displayDistance, distanceVal),
        temperatureCelsius = null,
        tagList = displayTags,
        travelTimeMinutes = null,
        openingHours = openingHours,
        isOpen = null,
        nextOpeningTime = null,
        accessibility = if (isAccessible == 1) "Accessible" else null,
        thumbnailLabel = safeName.take(1).ifBlank { "P" },
        thumbnailColors = defaultThumbnailColors(type),
        latitude = safeLatitude,
        longitude = safeLongitude,
        categories = baseCategories,
        imageUrl = imageUrl,
    )
}

private fun resolveDistanceMeters(displayDistance: String?, distanceVal: Double?): Int {
    parseDisplayDistanceMeters(displayDistance)?.let { return it }
    return distanceVal?.takeIf { it > 0.0 }?.times(1000)?.toInt() ?: 0
}

private fun parseDisplayDistanceMeters(displayDistance: String?): Int? {
    val normalized = displayDistance
        ?.trim()
        ?.lowercase()
        ?.replace(" ", "")
        ?.takeIf { it.isNotBlank() }
        ?: return null

    return when {
        normalized.endsWith("km") -> normalized.removeSuffix("km").toDoubleOrNull()?.times(1000)?.toInt()
        normalized.endsWith("m") -> normalized.removeSuffix("m").toDoubleOrNull()?.toInt()
        else -> null
    }
}

private fun defaultThumbnailColors(type: String?): List<Color> {
    return when (type?.lowercase()) {
        "library" -> listOf(Color(0xFFB67B32), Color(0xFF225A47), Color(0xFFE8D7A2))
        "park" -> listOf(Color(0xFF3C9B41), Color(0xFFF2CF38), Color(0xFFCE3750))
        "cooling centre" -> listOf(Color(0xFFC89D76), Color(0xFFF6E7D1), Color(0xFF8A5A43))
        else -> listOf(Color(0xFF9E8365), Color(0xFFE7D0B0), Color(0xFF6B5846))
    }
}

private fun toCoolPlaceCategory(value: String): CoolPlaceCategory? {
    return when (value.trim().lowercase()) {
        "indoor" -> CoolPlaceCategory.INDOOR
        "ac", "a/c" -> CoolPlaceCategory.AC
        "outdoor" -> CoolPlaceCategory.OUTDOOR
        "park" -> CoolPlaceCategory.PARK
        "water", "water_fountain", "water fountain" -> CoolPlaceCategory.WATER_FOUNTAIN
        "other" -> CoolPlaceCategory.OTHER
        else -> null
    }
}
