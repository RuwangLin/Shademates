package com.example.a5120_shademate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ShadeMateColorScheme = lightColorScheme(
    primary = Color(0xFF1F7A6D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9F0E8),
    onPrimaryContainer = Color(0xFF123D37),
    secondary = Color(0xFF68A99B),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFE88D3A),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF4F7F3),
    onBackground = Color(0xFF18312E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18312E),
    surfaceVariant = Color(0xFFEAF1EC),
    onSurfaceVariant = Color(0xFF5B6F69),
    outline = Color(0xFFD5DFD8),
)

private val ShadeMateTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
)

@Composable
fun ShadeMateTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ShadeMateColorScheme,
        typography = ShadeMateTypography,
        content = content,
    )
}
