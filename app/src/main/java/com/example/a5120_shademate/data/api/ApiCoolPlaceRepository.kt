package com.example.a5120_shademate.data.api

import android.util.Log
import com.example.a5120_shademate.data.CoolPlaceRepository
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiCoolPlaceRepository(
    private val fallbackRepository: CoolPlaceRepository,
    private val service: CoolPlacesApiService = AppApiClient.retrofit.create(CoolPlacesApiService::class.java),
) : CoolPlaceRepository {

    override suspend fun getCoolPlaces(
        lat: Double?,
        lon: Double?,
        limit: Int?,
        offset: Int?
    ): List<CoolPlaceCardData> = withContext(Dispatchers.IO) {
        // Keep the Cool Places flow available even if the live endpoint or mapper fails.
        runCatching {
            service.getCoolPlaces(
                lat = lat,
                lng = lon,
                limit = limit,
                offset = offset
            ).toCoolPlaceCardDataList()
        }
            .getOrElse { throwable ->
                Log.e("ApiCoolPlaces", "Falling back to sample content: ${throwable.message}", throwable)
                fallbackRepository.getCoolPlaces(lat, lon, limit, offset)
            }
    }
}
