package com.example.a5120_shademate.data

import com.example.a5120_shademate.data.api.CoolPlaceTemperatureResolver
import com.example.a5120_shademate.ui.components.CoolPlaceCardData

class TemperatureAwareCoolPlaceRepository(
    private val delegate: CoolPlaceRepository,
    private val temperatureResolver: CoolPlaceTemperatureResolver,
) : CoolPlaceRepository {

    override suspend fun getCoolPlaces(
        lat: Double?,
        lon: Double?,
        limit: Int?,
        offset: Int?
    ): List<CoolPlaceCardData> {
        val places = delegate.getCoolPlaces(lat = lat, lon = lon, limit = limit, offset = offset)
        return temperatureResolver.enrich(places)
    }
}
