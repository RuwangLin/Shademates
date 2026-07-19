package com.example.a5120_shademate.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CoolPlacesApiService {
    @GET("dev/getLocationData")
    suspend fun getCoolPlaces(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int? = 10,
        @Query("offset") offset: Int? = 0
    ): CoolPlacesResponse
}
