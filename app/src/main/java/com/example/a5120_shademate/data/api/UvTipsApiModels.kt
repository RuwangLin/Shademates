package com.example.a5120_shademate.data.api

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class UvTipsRequest(
    val location: String,
    @SerializedName("current_time") val currentTime: String,
    val temperature: String,
    @SerializedName("uv_index") val uvIndex: String,
    @SerializedName("age_group") val ageGroup: String,
    // This request keeps the original "skin_history" field name because the
    // backend contract still expects the payload shape below.
    @SerializedName("skin_history") val skinHistory: String,
)

data class UvTipsResponse(
    val statusCode: Int? = null,
    val headers: Map<String, String>? = null,
    val body: JsonElement? = null,
    val success: Boolean? = null,
    val tip: String? = null,
    @SerializedName("word_count") val wordCount: Int? = null,
)

private data class UvTipsPayload(
    val success: Boolean? = null,
    val tip: String? = null,
    @SerializedName("word_count") val wordCount: Int? = null,
)

internal fun UvTipsResponse.extractTip(gson: Gson = Gson()): String? {
    val directTip = tip?.takeIf { it.isNotBlank() }
    if (directTip != null && success != false) {
        return directTip
    }

    val responseBody = body ?: return null

    // The Lambda currently wraps the real payload in a JSON-encoded body string.
    val payload = when {
        responseBody.isJsonNull -> null
        responseBody.isJsonObject -> runCatching {
            gson.fromJson(responseBody, UvTipsPayload::class.java)
        }.getOrNull()
        responseBody.isJsonPrimitive && responseBody.asJsonPrimitive.isString -> runCatching {
            gson.fromJson(responseBody.asString, UvTipsPayload::class.java)
        }.getOrNull()
        else -> null
    }

    return payload
        ?.takeIf { it.success != false }
        ?.tip
        ?.takeIf { it.isNotBlank() }
}
