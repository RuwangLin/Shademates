package com.example.a5120_shademate.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a5120_shademate.R
import com.example.a5120_shademate.ui.theme.ShadeMateTheme
import kotlinx.coroutines.delay

private const val BannerAutoScrollMillis = 5000L

private data class BannerItem(
    val imageRes: Int,
    val title: String,
    val description: String,
)

private val mapBannerItems = listOf(
    BannerItem(
        imageRes = R.drawable.banner_heat_1,
        title = "Understand Heat\nAround You",
        description = "View real-time heat maps to see which areas are hotter or cooler.",
    ),
    BannerItem(
        imageRes = R.drawable.banner_heat_2,
        title = "Find Cooler\nRoutes",
        description = "Get smart route suggestions with more shade and less exposure.",
    ),
    BannerItem(
        imageRes = R.drawable.banner_heat_3,
        title = "Discover Nearby\nCool Places",
        description = "Find parks, libraries, shopping centers, and other cool spots near you.",
    ),
    BannerItem(
        imageRes = R.drawable.banner_heat_4,
        title = "Stay Safe\nin the Heat",
        description = "Receive heat alerts and helpful tips to protect your health every day.",
    ),
)

@Composable
fun MapBannerCarousel(
    modifier: Modifier = Modifier,
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentIndex) {
        delay(BannerAutoScrollMillis)
        currentIndex = if (currentIndex == mapBannerItems.lastIndex) 0 else currentIndex + 1
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        val compact = maxWidth <= 400.dp
        val narrow = maxWidth <= 360.dp
        val bannerHeight = when {
            narrow -> 296.dp
            compact -> 306.dp
            else -> 318.dp
        }

        Crossfade(
            targetState = currentIndex,
            label = "mapBannerCarousel",
        ) { index ->
            val banner = mapBannerItems[index]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight),
            ) {
                Image(
                    painter = painterResource(id = banner.imageRes),
                    contentDescription = banner.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x220B2B26),
                                    Color(0xCC0B2F29),
                                ),
                            ),
                        ),
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x500B2B26),
                                    Color(0x120B2B26),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = if (narrow) 16.dp else if (compact) 18.dp else 22.dp,
                            end = if (narrow) 16.dp else if (compact) 18.dp else 22.dp,
                            bottom = if (narrow) 48.dp else 54.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(if (narrow) 8.dp else 10.dp),
                ) {
                    Text(
                        text = banner.title,
                        modifier = Modifier.fillMaxWidth(
                            when {
                                narrow -> 0.88f
                                compact -> 0.82f
                                else -> 0.72f
                            }
                        ),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (narrow) 28.sp else if (compact) 30.sp else 34.sp,
                            lineHeight = if (narrow) 30.sp else if (compact) 32.sp else 36.sp,
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start,
                    )

                    Text(
                        text = banner.description,
                        modifier = Modifier.fillMaxWidth(
                            when {
                                narrow -> 1f
                                compact -> 0.96f
                                else -> 0.7f
                            }
                        ),
                        style = if (compact) {
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (narrow) 13.sp else 14.sp,
                                lineHeight = if (narrow) 17.sp else 19.sp,
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                            )
                        },
                        color = Color.White.copy(alpha = 0.96f),
                        maxLines = if (narrow) 3 else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (narrow) 14.dp else 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (narrow) 7.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    mapBannerItems.forEachIndexed { dotIndex, _ ->
                        Box(
                            modifier = Modifier
                                .size(
                                    if (dotIndex == currentIndex) {
                                        if (narrow) 11.dp else 12.dp
                                    } else {
                                        if (narrow) 9.dp else 10.dp
                                    }
                                )
                                .clip(CircleShape)
                                .background(
                                    if (dotIndex == currentIndex) MaterialTheme.colorScheme.primary
                                    else Color.White.copy(alpha = 0.75f),
                                )
                                .clickable { currentIndex = dotIndex },
                        )
                    }
                }
            }
        }

        CarouselButton(
            label = "<",
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = {
                currentIndex = if (currentIndex == 0) mapBannerItems.lastIndex else currentIndex - 1
            },
        )

        CarouselButton(
            label = ">",
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = {
                currentIndex = if (currentIndex == mapBannerItems.lastIndex) 0 else currentIndex + 1
            },
        )
    }
}

@Composable
private fun CarouselButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xE8141D1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                lineHeight = 20.sp,
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapBannerCarouselPreview() {
    ShadeMateTheme {
        MapBannerCarousel()
    }
}
