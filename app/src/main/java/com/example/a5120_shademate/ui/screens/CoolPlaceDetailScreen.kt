package com.example.a5120_shademate.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.ui.components.CenterTitleTopBar
import com.example.a5120_shademate.ui.components.CoolPlaceCardData
import com.example.a5120_shademate.ui.components.CoolPlaceImage
import kotlinx.coroutines.delay

@Composable
fun CoolPlaceDetailScreen(
    place: CoolPlaceCardData,
    onBack: () -> Unit,
    onNavigate: (CoolPlaceCardData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        CenterTitleTopBar(
            title = "Cool Place Info",
            onBack = onBack,
            titleColor = Color(0xFF55575A),
            modifier = Modifier.detailEntrance(
                animationKey = "${place.id}:header",
                index = 0,
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .detailEntrance(
                    animationKey = "${place.id}:hero",
                    index = 1,
                ),
            contentAlignment = Alignment.BottomStart,
        ) {
            CoolPlaceImage(
                place = place,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.52f)),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailChip(place.type)
                    DetailChip(place.distanceText)
                    DetailChip(place.temperatureText)
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailInfoCard(
                title = "Overview",
                lines = listOf(
                    "${place.suburb} ${place.type}",
                    place.tagsText,
                ),
                modifier = Modifier.detailEntrance(
                    animationKey = "${place.id}:overview",
                    index = 2,
                ),
            )

            DetailInfoCard(
                title = "Opening Hours",
                lines = buildList {
                    // Some live place payloads are partial, so the detail view must tolerate missing fields.
                    add(place.openingHours ?: "Hours unavailable")
                    when (place.isOpen) {
                        true -> add("Status: Open now")
                        false -> {
                            add("Status: Closed")
                            place.nextOpeningTime?.let { add(it) }
                        }
                        null -> add("Status: Availability unavailable")
                    }
                },
                modifier = Modifier.detailEntrance(
                    animationKey = "${place.id}:hours",
                    index = 3,
                ),
            )

            DetailInfoCard(
                title = "Accessibility",
                lines = listOf(place.accessibility ?: "Accessibility information is not available yet."),
                modifier = Modifier.detailEntrance(
                    animationKey = "${place.id}:accessibility",
                    index = 4,
                ),
            )

            Button(
                onClick = { onNavigate(place) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .detailEntrance(
                        animationKey = "${place.id}:cta",
                        index = 5,
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF477A31)),
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Get Directions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DetailChip(label: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.18f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E2E2)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF55575A),
                fontWeight = FontWeight.ExtraBold,
            )

            lines.filter { it.isNotBlank() }.forEachIndexed { index, line ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    val icon = when (title) {
                        "Opening Hours" -> Icons.Default.Schedule
                        else -> Icons.Default.LocationOn
                    }
                    if (index == 0) {
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF477A31),
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF55575A),
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.detailEntrance(
    animationKey: Any,
    index: Int,
): Modifier {
    var visible by remember(animationKey) { mutableStateOf(false) }

    LaunchedEffect(animationKey) {
        visible = false
        delay(index * 90L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "cool_place_detail_alpha",
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "cool_place_detail_translation",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.985f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "cool_place_detail_scale",
    )

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
        scaleX = scale
        scaleY = scale
    }
}
