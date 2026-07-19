package com.example.a5120_shademate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a5120_shademate.ui.theme.ShadeMatePalette

@Composable
fun ShadeMateTopBar(
    modifier: Modifier = Modifier,
    title: String = "ShadeMate",
    titleColor: Color = ShadeMatePalette.PrimaryGreen,
    titleSize: TextUnit = 32.sp,
    containerColor: Color = ShadeMatePalette.AppBackground,
    iconTint: Color = ShadeMatePalette.PrimaryText,
    profileTint: Color = ShadeMatePalette.PrimaryGreen,
    iconContainerColor: Color = ShadeMatePalette.CardBackground.copy(alpha = 0.78f),
    horizontalPadding: Dp = 20.dp,
    onProfileClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShadeMateProfileButton(
                profileTint = profileTint,
                iconContainerColor = iconContainerColor,
                onClick = onProfileClick,
            )
        }
    }
}

@Composable
fun ShadeMateProfileButton(
    modifier: Modifier = Modifier,
    profileTint: Color = ShadeMatePalette.PrimaryGreen,
    iconContainerColor: Color = ShadeMatePalette.CardBackground.copy(alpha = 0.78f),
    iconSize: Dp = 24.dp,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = iconContainerColor,
        tonalElevation = 2.dp,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = profileTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
fun CenterTitleTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = ShadeMatePalette.PrimaryText,
    titleSize: TextUnit = 32.sp,
    showBackButton: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ShadeMatePalette.AppBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ShadeMatePalette.PrimaryText,
                        modifier = Modifier.size(30.dp),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = titleSize),
                color = titleColor,
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}
