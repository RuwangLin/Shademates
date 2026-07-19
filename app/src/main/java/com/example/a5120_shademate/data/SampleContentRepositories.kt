package com.example.a5120_shademate.data

import androidx.compose.ui.graphics.Color
import com.example.a5120_shademate.R
import com.example.a5120_shademate.model.CurrentLocationWeather
import com.example.a5120_shademate.model.HomeAreaTemperature
import com.example.a5120_shademate.model.HomeHeatOverview
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.example.a5120_shademate.ui.components.CoolPlaceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SampleCoolPlaceRepository : CoolPlaceRepository {
    override suspend fun getCoolPlaces(
        lat: Double?,
        lon: Double?,
        limit: Int?,
        offset: Int?
    ): List<CoolPlaceCardData> = withContext(Dispatchers.Default) {
        listOf(
            CoolPlaceCardData(
                id = "state_library",
                name = "State Library",
                suburb = "Melbourne CBD",
                type = "Library",
                distanceMeters = 1300,
                temperatureCelsius = 25,
                tagList = listOf("A/C available", "Drinking water available", "Bench available"),
                travelTimeMinutes = 10,
                openingHours = "10:00 AM - 6:00 PM",
                isOpen = true,
                nextOpeningTime = null,
                accessibility = "Wheelchair access available",
                thumbnailLabel = "Library",
                thumbnailColors = listOf(Color(0xFFB67B32), Color(0xFF225A47), Color(0xFFE8D7A2)),
                latitude = -37.8097,
                longitude = 144.9653,
                categories = setOf(
                    CoolPlaceCategory.INDOOR,
                    CoolPlaceCategory.AC,
                    CoolPlaceCategory.WATER_FOUNTAIN,
                ),
                thumbnailRes = R.drawable.place_state_library,
            ),
            CoolPlaceCardData(
                id = "carlton_gardens",
                name = "Carlton Gardens",
                suburb = "Carlton",
                type = "Park",
                distanceMeters = 1900,
                temperatureCelsius = 23,
                tagList = listOf("Outdoor shade", "Drinking water available", "Bench available"),
                travelTimeMinutes = 13,
                openingHours = "Open 24 hours",
                isOpen = true,
                nextOpeningTime = null,
                accessibility = "Level paths and accessible toilets nearby",
                thumbnailLabel = "Gardens",
                thumbnailColors = listOf(Color(0xFF3C9B41), Color(0xFFF2CF38), Color(0xFFCE3750)),
                latitude = -37.8060,
                longitude = 144.9717,
                categories = setOf(
                    CoolPlaceCategory.OUTDOOR,
                    CoolPlaceCategory.PARK,
                    CoolPlaceCategory.WATER_FOUNTAIN,
                ),
                thumbnailRes = R.drawable.place_carlton_gardens,
            ),
            CoolPlaceCardData(
                id = "qv_atrium",
                name = "QV Atrium",
                suburb = "Flagstaff",
                type = "Cooling Centre",
                distanceMeters = 2600,
                temperatureCelsius = 24,
                tagList = listOf("A/C available", "Drinking water available", "Bench available"),
                travelTimeMinutes = 20,
                openingHours = "9:00 AM - 9:00 PM",
                isOpen = false,
                nextOpeningTime = "Opens tomorrow at 9:00 AM",
                accessibility = "Lift access available",
                thumbnailLabel = "Atrium",
                thumbnailColors = listOf(Color(0xFFC89D76), Color(0xFFF6E7D1), Color(0xFF8A5A43)),
                latitude = -37.8107,
                longitude = 144.9647,
                categories = setOf(
                    CoolPlaceCategory.INDOOR,
                    CoolPlaceCategory.AC,
                    CoolPlaceCategory.WATER_FOUNTAIN,
                ),
                thumbnailRes = R.drawable.place_qv_atrium,
            ),
            CoolPlaceCardData(
                id = "emporium",
                name = "Emporium",
                suburb = "Melbourne CBD",
                type = "Shopping Centre",
                distanceMeters = 2100,
                temperatureCelsius = 26,
                tagList = listOf("Indoor route", "A/C available", "Seating nearby"),
                travelTimeMinutes = 12,
                openingHours = "10:00 AM - 7:00 PM",
                isOpen = true,
                nextOpeningTime = null,
                accessibility = "Wheelchair access and lifts available",
                thumbnailLabel = "Mall",
                thumbnailColors = listOf(Color(0xFF9E8365), Color(0xFFE7D0B0), Color(0xFF6B5846)),
                latitude = -37.8118,
                longitude = 144.9649,
                categories = setOf(
                    CoolPlaceCategory.INDOOR,
                    CoolPlaceCategory.AC,
                ),
            ),
        )
    }
}

class SampleHomeOverviewRepository : HomeOverviewRepository {
    override suspend fun getHomeHeatOverview(lat: Double?, lon: Double?): HomeHeatOverview = withContext(Dispatchers.Default) {
        HomeHeatOverview(
            uvIndex = 7,
            uvLabel = "VERY HIGH",
            areaTemperatures = listOf(
                HomeAreaTemperature("Melbourne CBD", 12, 21, 36),
                HomeAreaTemperature("Footscray", 16, 22, 34),
                HomeAreaTemperature("Brunswick", 15, 24, 37),
            ),
            quickTips = listOf(
                "Limit unprotected exposure to 15 minutes",
                "Apply sunscreen with SPF30",
                "Stay hydrated. Drink water every 20 minutes",
            ),
            currentLocationWeather = CurrentLocationWeather(
                areaName = "Melbourne CBD",
                currentCelsius = 21,
                lowCelsius = 12,
                highCelsius = 36,
                feelsLikeCelsius = 24,
                shadeCoverageScore = 82,
                heatExposureScore = 37,
            ),
        )
    }
}
