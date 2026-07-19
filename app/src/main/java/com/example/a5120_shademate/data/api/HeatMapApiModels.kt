package com.example.a5120_shademate.data.api

import com.google.gson.annotations.SerializedName

// --- 首页评分接口模型 ---
data class HeatMapAverageRequest(
    val lat: Double,
    val lon: Double,
    @SerializedName("current_month") val currentMonth: Int,
    @SerializedName("current_hour") val currentHour: Int
)

data class HeatMapAverageResponse(
    val suburb: String? = null,
    @SerializedName("sun_exposure_ratio") val sunExposureRatio: Double? = null
)

// --- 地图图层接口模型 ---
data class HeatMapRequest(
    val bbox: List<Double>,
    @SerializedName("current_month") val currentMonth: Int,  // 统一字段名
    @SerializedName("current_hour") val currentHour: Int    // 统一字段名
)

data class HeatMapResponse(
    val statusCode: Int,
    val body: String // 存储转义后的 JSON 字符串
)

data class HeatMapResponseBody(
    @SerializedName("s3_url") val s3Url: String,
    val suburb: String,
    @SerializedName("sun_exposure_ratio") val sunExposureRatio: Double
)
