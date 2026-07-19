package com.example.a5120_shademate.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UvTipsApiService {
    @POST("dev/postUVTips")
    suspend fun postUvTips(
        @Header("x-api-key") apiKey: String,
        @Body request: UvTipsRequest,
    ): UvTipsResponse
}
