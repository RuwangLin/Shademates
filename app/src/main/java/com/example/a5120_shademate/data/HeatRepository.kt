package com.example.a5120_shademate.data

import com.example.a5120_shademate.model.EducationContent
import com.example.a5120_shademate.model.HeatMapData

/**
 * Keep this interface stable.
 * Future backend work should implement this contract so the Compose UI can stay unchanged.
 */
interface HeatRepository {
    suspend fun getHeatMapData(): HeatMapData

    suspend fun getEducationContent(): List<EducationContent>
}
