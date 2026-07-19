package com.example.a5120_shademate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a5120_shademate.model.HeatLevel
import com.example.a5120_shademate.ui.theme.ShadeMateTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeatLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Heat levels",
                style = MaterialTheme.typography.titleMedium,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeatLegendItem(level = HeatLevel.COOL)
                HeatLegendItem(level = HeatLevel.WARM)
                HeatLegendItem(level = HeatLevel.HOT)
                HeatLegendItem(level = HeatLevel.EXTREME)
            }
        }
    }
}

@Composable
private fun HeatLegendItem(level: HeatLevel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .background(colorForHeatLevel(level), CircleShape),
            )
            Text(
                text = level.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

fun colorForHeatLevel(level: HeatLevel): Color {
    return when (level) {
        HeatLevel.COOL -> Color(0xFF4EB9C8)
        HeatLevel.WARM -> Color(0xFFF2B44A)
        HeatLevel.HOT -> Color(0xFFF07A42)
        HeatLevel.EXTREME -> Color(0xFFDB574B)
    }
}

@Preview(showBackground = true)
@Composable
private fun HeatLegendPreview() {
    ShadeMateTheme {
        HeatLegend(modifier = Modifier.wrapContentHeight())
    }
}
