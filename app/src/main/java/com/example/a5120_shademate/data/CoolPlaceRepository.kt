package com.example.a5120_shademate.data

import com.example.a5120_shademate.ui.components.CoolPlaceCardData

interface CoolPlaceRepository {
    suspend fun getCoolPlaces(
        lat: Double? = null,
        lon: Double? = null,
        limit: Int? = null,
        offset: Int? = null
    ): List<CoolPlaceCardData>
}
