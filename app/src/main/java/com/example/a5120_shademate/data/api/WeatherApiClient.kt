package com.example.a5120_shademate.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object WeatherApiClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/3.0/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
