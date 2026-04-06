package com.wisdometer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    background = Background,
    surface = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
)

@Composable
fun WisdometerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = WisdometerTypography,
        content = content,
    )
}
