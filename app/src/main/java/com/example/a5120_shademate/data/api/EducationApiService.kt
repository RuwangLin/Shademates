package com.example.a5120_shademate.data.api

import retrofit2.http.GET

interface EducationApiService {
    @GET("dev/getEducationData")
    suspend fun getEducationData(): EducationApiResponse
}
