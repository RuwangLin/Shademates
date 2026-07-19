package com.example.a5120_shademate.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.ui.theme.ShadeMatePalette

enum class CoolPlaceCategory(val label: String) {
    INDOOR("Indoor"),
    AC("A/C"),
    OUTDOOR("Outdoor"),
    PARK("Park"),
    WATER_FOUNTAIN("Water"),
    OTHER("Other"),
}

data class CoolPlaceCardData(
    val id: String,
    val name: String,
    val suburb: String,
    val type: String,
    val distanceMeters: Int,
    val temperatureCelsius: Int? = null,
    val tagList: List<String> = emptyList(),
    val travelTimeMinutes: Int? = null,
    val openingHours: String? = null,
    val isOpen: Boolean? = null,
    val nextOpeningTime: String?,
    val accessibility: String?,
    val thumbnailLabel: String,
    val thumbnailColors: List<Color>,
    val latitude: Double,
    val longitude: Double,
    val categories: Set<CoolPlaceCategory> = emptySet(),
    val imageUrl: String? = null,
    @DrawableRes val thumbnailRes: Int? = null,
) {
    val distanceText: String
        get() = if (distanceMeters >= 1000) {
            String.format("%.1f km away", distanceMeters / 1000f)
        } else {
            "$distanceMeters m away"
        }

    val temperatureText: String
        get() = temperatureCelsius?.let { "${it}\u00B0C" } ?: "Temp unavailable"

    val tagsText: String
        get() = tagList.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Amenities information coming soon"

    val travelTimeText: String
        get() = travelTimeMinutes?.let { "$it min" } ?: "N/A"
}

@Composable
fun CoolPlaceCard(
    place: CoolPlaceCardData,
    onClick: (() -> Unit)? = null,
    onGoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val openPlace = onGoClick ?: onClick

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(326.dp)
            .then(if (openPlace != null) Modifier.clickable(onClick = openPlace) else Modifier),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ShadeMatePalette.CardBackground),
        border = BorderStroke(1.dp, ShadeMatePalette.Border.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            ) {
                CoolPlaceImage(
                    place = place,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = ShadeMatePalette.PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = place.suburb,
                        style = MaterialTheme.typography.titleMedium,
                        color = ShadeMatePalette.SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = place.tagList.take(3).joinToString(" | ").ifBlank { place.tagsText },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShadeMatePalette.SecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ShadeMatePalette.SelectedNavBackground,
                    ) {
                        Text(
                            text = place.distanceText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = ShadeMatePalette.PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = ShadeMatePalette.PrimaryGreen,
                    onClick = { openPlace?.invoke() },
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open cool place",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}
