package com.wisdometer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    background = Background,
    surface = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onSurfaceVariant = SecondaryText,
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF121210),
    surface = Color(0xFF1E1E1C),
    onBackground = Color(0xFFEAEAE8),
    onSurface = Color(0xFFEAEAE8),
    onSurfaceVariant = Color(0xFF9A9A9A),
    surfaceVariant = Color(0xFF2A2A28),
)

@Composable
fun WisdometerTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = if (isDark) DarkColors else LightColors
    val wisdometerColors = if (isDark) darkWisdometerColors() else lightWisdometerColors()

    CompositionLocalProvider(LocalWisdometerColors provides wisdometerColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WisdometerTypography,
            content = content,
        )
    }
}
