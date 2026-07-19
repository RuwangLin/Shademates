package com.example.a5120_shademate.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface HeatMapApiService {
    @POST("dev/postHeatMap/postAverage")
    suspend fun getHeatMapAverage(
        @Body request: HeatMapAverageRequest
    ): HeatMapAverageResponse

    @POST("dev/postHeatMap")
    suspend fun getHeatMap(
        @Body request: HeatMapRequest
    ): HeatMapResponse
}
