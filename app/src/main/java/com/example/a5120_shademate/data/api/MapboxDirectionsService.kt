package com.example.a5120_shademate.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapboxDirectionsService {
    @GET("directions/v5/mapbox/walking/{coordinates}")
    suspend fun getWalkingRoutes(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full",
        @Query("steps") steps: Boolean = false,
        @Query("access_token") accessToken: String,
    ): DirectionsResponse
}

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<DirectionsRoute>,
)

data class DirectionsRoute(
    @SerializedName("geometry") val geometry: DirectionsGeometry,
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
)

data class DirectionsGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>,
)
