package com.example.a5120_shademate.data

import android.content.Context

data class UserProfile(
    val ageGroup: String,
    // Kept as "skinHistory" for now so local naming stays aligned with the
    // current backend contract. The Customise UI currently stores the
    // heat-sensitivity selector answer in this field.
    val skinHistory: String,
)

object UserProfilePreferences {
    const val prefsName = "user_profile"
    const val ageGroupKey = "age_group"
    const val skinHistoryKey = "skin_history"
    const val defaultAgeGroup = "25-34"
    const val defaultSkinHistory = "No, I handle heat well."

    val ageGroupOptions = listOf(
        "Under 18",
        "18-24",
        "25-34",
        "35-44",
        "45-54",
        "55-64",
        "65+",
    )

    fun read(context: Context): UserProfile {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return UserProfile(
            ageGroup = prefs.getString(ageGroupKey, null)
                ?.takeIf { it.isNotBlank() }
                ?: defaultAgeGroup,
            skinHistory = prefs.getString(skinHistoryKey, null)
                ?.takeIf { it.isNotBlank() }
                ?: defaultSkinHistory,
        )
    }

    fun write(context: Context, profile: UserProfile) {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putString(ageGroupKey, profile.ageGroup)
            .putString(skinHistoryKey, profile.skinHistory)
            .apply()
    }
}
