package com.wisdometer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val AppColors = darkColorScheme(
    background = Color(0xFF222222),
    surface = Color(0xFF2E2E2E),
    onBackground = Color(0xFFEAEAE8),
    onSurface = Color(0xFFEAEAE8),
    onSurfaceVariant = Color(0xFF9A9A9A),
    surfaceVariant = Color(0xFF383838),
)

@Composable
fun WisdometerTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalWisdometerColors provides darkWisdometerColors()) {
        MaterialTheme(
            colorScheme = AppColors,
            typography = WisdometerTypography,
            content = content,
        )
    }
}
