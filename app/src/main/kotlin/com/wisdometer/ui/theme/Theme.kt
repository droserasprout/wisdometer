package com.wisdometer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val AppColors = darkColorScheme(
    background = Color(0xFF222222),
    surface = Color(0xFF2E2B28),
    surfaceVariant = Color(0xFF3A3632),
    onBackground = Color(0xFFEAEAE8),
    onSurface = Color(0xFFEAEAE8),
    onSurfaceVariant = Color(0xFFB0B0B0),
    primary = Color(0xFFB8C4D1),
    onPrimary = Color(0xFF1A1F24),
    secondary = Color(0xFFC8B8A8),
    onSecondary = Color(0xFF1F1A14),
    error = SemanticColors.red,
    onError = Color(0xFF1A0D0D),
    outline = Color(0xFF5A5652),
    outlineVariant = Color(0xFF3A3632),
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
