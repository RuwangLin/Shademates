package com.example.a5120_shademate.data.api

import com.example.a5120_shademate.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object AppApiClient {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
