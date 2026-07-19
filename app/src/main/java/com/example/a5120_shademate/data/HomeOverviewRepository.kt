package com.example.a5120_shademate.data

import com.example.a5120_shademate.model.HomeHeatOverview

interface HomeOverviewRepository {
    suspend fun getHomeHeatOverview(lat: Double? = null, lon: Double? = null): HomeHeatOverview
}
