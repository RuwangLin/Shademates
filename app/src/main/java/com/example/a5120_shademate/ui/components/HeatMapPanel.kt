package com.example.a5120_shademate.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.model.HeatLevel
import com.example.a5120_shademate.model.HeatZone
import com.example.a5120_shademate.ui.theme.ShadeMateTheme

@Composable
fun HeatMapPanel(
    cityName: String,
    zones: List<HeatZone>,
    modifier: Modifier = Modifier,
) {
    val hottestZone = zones.maxByOrNull { it.temperatureCelsius }
    val averageTemp = zones.map { it.temperatureCelsius }.average().toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF6FBF8),
                                Color(0xFFE6F3EF),
                                Color(0xFFEAF1F8),
                            ),
                        ),
                    ),
            ) {
                MelbourneBackdrop()

                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.84f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Mock heat overlay and floating temperature tags",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            Text(
                                text = "MAP",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                zones.forEach { zone ->
                    HeatZoneMarker(
                        zone = zone,
                        modifier = Modifier.offset(
                            x = maxWidth * zone.xRatio - 34.dp,
                            y = maxHeight * zone.yRatio - 18.dp,
                        ),
                    )
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Average heat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "$averageTemp C",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Hottest zone",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = hottestZone?.let { "${it.name} ${it.temperatureCelsius} C" } ?: "No data",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MelbourneBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridColor = Color.White.copy(alpha = 0.7f)
        val outlineColor = Color(0xFF9BCABD)
        val routeColor = Color(0xFF34A37E)

        repeat(7) { row ->
            val y = size.height * (row + 1) / 8f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3f,
            )
        }

        repeat(5) { column ->
            val x = size.width * (column + 1) / 6f
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 3f,
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFC884).copy(alpha = 0.65f), Color.Transparent),
                center = Offset(size.width * 0.72f, size.height * 0.46f),
                radius = size.minDimension * 0.34f,
            ),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.72f, size.height * 0.46f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFA76D).copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * 0.80f, size.height * 0.58f),
                radius = size.minDimension * 0.26f,
            ),
            radius = size.minDimension * 0.26f,
            center = Offset(size.width * 0.80f, size.height * 0.58f),
        )

        val bayPath = Path().apply {
            moveTo(size.width * 0.55f, size.height * 0.69f)
            quadraticTo(
                size.width * 0.78f,
                size.height * 0.88f,
                size.width,
                size.height,
            )
            lineTo(size.width, size.height * 0.58f)
            quadraticTo(
                size.width * 0.82f,
                size.height * 0.57f,
                size.width * 0.55f,
                size.height * 0.69f,
            )
            close()
        }
        drawPath(
            path = bayPath,
            color = Color(0xFFA8D7F4).copy(alpha = 0.55f),
        )

        val metroOutline = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.26f)
            quadraticTo(
                size.width * 0.36f,
                size.height * 0.06f,
                size.width * 0.62f,
                size.height * 0.18f,
            )
            quadraticTo(
                size.width * 0.93f,
                size.height * 0.33f,
                size.width * 0.86f,
                size.height * 0.75f,
            )
            quadraticTo(
                size.width * 0.64f,
                size.height * 0.92f,
                size.width * 0.30f,
                size.height * 0.84f,
            )
            quadraticTo(
                size.width * 0.08f,
                size.height * 0.63f,
                size.width * 0.14f,
                size.height * 0.26f,
            )
            close()
        }
        drawPath(
            path = metroOutline,
            color = outlineColor,
            style = Stroke(width = 5f),
        )

        val routePath = Path().apply {
            moveTo(size.width * 0.16f, size.height * 0.68f)
            quadraticTo(
                size.width * 0.34f,
                size.height * 0.52f,
                size.width * 0.46f,
                size.height * 0.58f,
            )
            quadraticTo(
                size.width * 0.60f,
                size.height * 0.65f,
                size.width * 0.82f,
                size.height * 0.44f,
            )
        }
        drawPath(
            path = routePath,
            color = routeColor.copy(alpha = 0.9f),
            style = Stroke(width = 10f),
        )
    }
}

@Composable
private fun HeatZoneMarker(
    zone: HeatZone,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorForHeatLevel(zone.level)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "${zone.temperatureCelsius} C",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = zone.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeatMapPanelPreview() {
    ShadeMateTheme {
        HeatMapPanel(
            cityName = "Melbourne",
            zones = listOf(
                HeatZone("1", "CBD", 36, HeatLevel.EXTREME, 0.5f, 0.40f, "Preview"),
                HeatZone("2", "Docklands", 33, HeatLevel.HOT, 0.76f, 0.34f, "Preview"),
                HeatZone("3", "St Kilda", 27, HeatLevel.COOL, 0.62f, 0.74f, "Preview"),
            ),
        )
    }
}
