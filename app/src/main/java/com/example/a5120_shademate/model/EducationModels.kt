package com.example.a5120_shademate.model

/**
 * Represents the content_type enum in the database.
 */
enum class ContentType {
    STATISTIC, FACT, RECORD
}

/**
 * Represents the category enum in the database.
 */
enum class EducationCategory {
    HEALTH_IMPACT,
    ECONOMIC_IMPACT,
    CLIMATE_RECORD,
    URBAN_HEAT,
    VULNERABILITY,
    FUTURE_RISK,
    UV_EXPOSURE
}

/**
 * Core education content data class, mapping the database table structure.
 */
data class EducationContent(
    val contentId: Int,
    val title: String,
    val contentType: ContentType,
    val category: EducationCategory,
    val description: String,
    val sourceName: String? = null,
    val mediaUrl: String? = null,
    val createdAt: String? = null // Maps to the date type; formatting in the business layer is recommended.
)
