package com.example.a5120_shademate.data.api

import com.example.a5120_shademate.BuildConfig
import com.example.a5120_shademate.data.HeatRepository
import com.example.a5120_shademate.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiHeatRepository : HeatRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(EducationApiService::class.java)
    private val gson = Gson()

    override suspend fun getHeatMapData(): HeatMapData = withContext(Dispatchers.IO) {
        // Keeping mock for now as per instructions, or we could implement a real one if URL is provided
        HeatMapData(
            cityName = "Melbourne",
            lastUpdated = "Live API",
            zones = listOf(
                HeatZone("cbd", "CBD", 36, HeatLevel.EXTREME, 0.52f, 0.42f, "Dense urban heat around the city core."),
                HeatZone("footscray", "Footscray", 33, HeatLevel.HOT, 0.28f, 0.44f, "Hot conditions across western suburbs."),
                HeatZone("brunswick", "Brunswick", 30, HeatLevel.WARM, 0.48f, 0.24f, "Warm afternoon temperatures continuing north."),
                HeatZone("st-kilda", "St Kilda", 27, HeatLevel.COOL, 0.60f, 0.72f, "Cooler coastal air near Port Phillip Bay."),
                HeatZone("dandenong", "Dandenong", 32, HeatLevel.HOT, 0.84f, 0.58f, "Sustained heat across the south-east corridor.")
            )
        )
    }

    override suspend fun getEducationContent(): List<EducationContent> = withContext(Dispatchers.IO) {
        val response = service.getEducationData()

        // The API returns a JSON string inside the 'body' field.
        val itemType = object : TypeToken<List<EducationApiItem>>() {}.type
        val apiItems: List<EducationApiItem> = gson.fromJson(response.body, itemType)

        // Map API items to our UI Model
        apiItems.map { item ->
            EducationContent(
                contentId = item.contentId,
                title = item.title,
                contentType = when (item.contentType.lowercase()) {
                    "statistic" -> ContentType.STATISTIC
                    "fact" -> ContentType.FACT
                    "record" -> ContentType.RECORD
                    else -> ContentType.FACT
                },
                category = when (item.category.lowercase()) {
                    "vulnerability" -> EducationCategory.VULNERABILITY
                    "health_impact" -> EducationCategory.HEALTH_IMPACT
                    "urban_heat" -> EducationCategory.URBAN_HEAT
                    "climate_record" -> EducationCategory.CLIMATE_RECORD
                    "future_risk" -> EducationCategory.FUTURE_RISK
                    "economic_impact" -> EducationCategory.ECONOMIC_IMPACT
                    "uv_exposure" -> EducationCategory.UV_EXPOSURE
                    else -> EducationCategory.HEALTH_IMPACT
                },
                description = item.description,
                sourceName = item.sourceName,
                mediaUrl = item.mediaUrl,
                createdAt = item.createdAt
            )
        }
    }
}
