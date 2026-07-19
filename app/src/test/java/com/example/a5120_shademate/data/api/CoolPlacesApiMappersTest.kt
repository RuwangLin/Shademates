package com.example.a5120_shademate.data.api

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CoolPlacesApiMappersTest {

    @Test
    fun `uses display distance when provided`() {
        val response = CoolPlacesResponse(
            data = CoolPlacesData(
                items = listOf(
                    coolPlaceDto(
                        distanceVal = 0.11611390331705851,
                        displayDistance = "116m",
                    )
                )
            )
        )

        val place = response.toCoolPlaceCardDataList().single()

        assertEquals(116, place.distanceMeters)
    }

    @Test
    fun `converts distance value from kilometers to meters when display distance missing`() {
        val response = CoolPlacesResponse(
            data = CoolPlacesData(
                items = listOf(
                    coolPlaceDto(
                        distanceVal = 1.8,
                        displayDistance = null,
                    )
                )
            )
        )

        val place = response.toCoolPlaceCardDataList().single()

        assertEquals(1800, place.distanceMeters)
    }

    private fun coolPlaceDto(
        distanceVal: Double,
        displayDistance: String?,
    ) = CoolPlaceDto(
        id = 1,
        name = "Test Place",
        type = "Park",
        latitude = -37.8136,
        longitude = 144.9631,
        suburb = "Melbourne",
        distanceVal = distanceVal,
        displayDistance = displayDistance,
        filters = JsonParser.parseString("[\"park\"]"),
    )
}
