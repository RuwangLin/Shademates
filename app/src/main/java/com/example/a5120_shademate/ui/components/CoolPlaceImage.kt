package com.example.a5120_shademate.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.a5120_shademate.R

@Composable
fun CoolPlaceImage(
    place: CoolPlaceCardData,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val fallbackRes = place.resolvedThumbnailRes
    val remoteImageUrl = place.remoteImageUrl

    when {
        remoteImageUrl != null -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(remoteImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = place.name,
                modifier = modifier,
                contentScale = contentScale,
                error = fallbackRes?.let { painterResource(it) },
                fallback = fallbackRes?.let { painterResource(it) },
                placeholder = fallbackRes?.let { painterResource(it) },
            )
        }

        fallbackRes != null -> {
            Image(
                painter = painterResource(fallbackRes),
                contentDescription = place.name,
                modifier = modifier,
                contentScale = contentScale,
            )
        }

        else -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(place.thumbnailColors)),
            )
        }
    }
}

val CoolPlaceCardData.remoteImageUrl: String?
    get() = imageUrl
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let { rawUrl ->
            when {
                rawUrl.startsWith("https://", ignoreCase = true) -> rawUrl
                rawUrl.startsWith("http://", ignoreCase = true) -> rawUrl
                rawUrl.startsWith("//") -> "https:$rawUrl"
                rawUrl.startsWith("www.", ignoreCase = true) -> "https://$rawUrl"
                else -> null
            }
        }

val CoolPlaceCardData.resolvedThumbnailRes: Int?
    @DrawableRes
    get() = thumbnailRes ?: fallbackThumbnailRes()

@DrawableRes
private fun CoolPlaceCardData.fallbackThumbnailRes(): Int {
    val normalizedType = type.lowercase()
    return when {
        normalizedType.contains("park") ||
            normalizedType.contains("garden") ||
            normalizedType.contains("reserve") ||
            categories.contains(CoolPlaceCategory.PARK) ||
            categories.contains(CoolPlaceCategory.OUTDOOR) -> R.drawable.place_carlton_gardens

        normalizedType.contains("library") ||
            normalizedType.contains("school") ||
            normalizedType.contains("education") -> R.drawable.place_state_library

        normalizedType.contains("centre") ||
            normalizedType.contains("center") ||
            normalizedType.contains("shopping") ||
            normalizedType.contains("mall") ||
            normalizedType.contains("indoor") ||
            categories.contains(CoolPlaceCategory.INDOOR) ||
            categories.contains(CoolPlaceCategory.AC) -> R.drawable.place_qv_atrium

        else -> R.drawable.place_carlton_gardens
    }
}
