package com.example.a5120_shademate.data.api

import com.google.gson.annotations.SerializedName

/**
 * The raw response wrapper from the AWS Lambda API.
 */
data class EducationApiResponse(
    val statusCode: Int,
    val body: String // The body is a JSON string that needs secondary parsing
)

/**
 * The structure of each item within the 'body' JSON string.
 */
data class EducationApiItem(
    @SerializedName("content_id") val contentId: Int,
    val title: String,
    @SerializedName("content_type") val contentType: String,
    val category: String,
    val description: String,
    @SerializedName("source_name") val sourceName: String?,
    @SerializedName("media_url") val mediaUrl: String?,
    @SerializedName("created_at") val createdAt: String?
)
