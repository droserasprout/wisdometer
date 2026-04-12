package com.wisdometer.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Light palette
val Background = Color(0xFFFAFAF8)
val CardBackground = Color(0xFFFFFFFF)
val CardShadow = Color(0x1A000000)
val PrimaryText = Color(0xFF1A1A1A)
val SecondaryText = Color(0xFF6B6B6B)
val DividerColor = Color(0xFFE8E8E4)

val BadgeOpenBackground = Color(0xFFFFF3CD)
val BadgeOpenText = Color(0xFF856404)
val BadgeResolvedBackground = Color(0xFFD4EDDA)
val BadgeResolvedText = Color(0xFF155724)

val BarColors = listOf(
    Color(0xFF4A90D9),
    Color(0xFF7EC8A4),
    Color(0xFFE8A44A),
    Color(0xFFD96A6A),
    Color(0xFF9B7EC8),
    Color(0xFF4AC8C8),
)

/** Red (weight 1) → Yellow (weight 5) → Green (weight 10). */
fun weightColor(weight: Int): Color {
    val t = (weight - 1).coerceIn(0, 9) / 9f
    val red = Color(0xFFD96A6A)
    val amber = Color(0xFFE8A44A)
    val green = Color(0xFF5CB85C)
    return if (t < 0.5f) lerp(red, amber, t * 2f) else lerp(amber, green, (t - 0.5f) * 2f)
}

// Custom colors that aren't covered by Material3 colorScheme
data class WisdometerColors(
    val badgeOpenBackground: Color,
    val badgeOpenText: Color,
    val badgeResolvedBackground: Color,
    val badgeResolvedText: Color,
    val chartGridLine: Color,
    val resolvedCardAlpha: Float,
)

fun lightWisdometerColors() = WisdometerColors(
    badgeOpenBackground = BadgeOpenBackground,
    badgeOpenText = BadgeOpenText,
    badgeResolvedBackground = BadgeResolvedBackground,
    badgeResolvedText = BadgeResolvedText,
    chartGridLine = Color(0xFFE0E0E0),
    resolvedCardAlpha = 0.5f,
)

fun darkWisdometerColors() = WisdometerColors(
    badgeOpenBackground = Color(0xFF3D2E00),
    badgeOpenText = Color(0xFFFFD666),
    badgeResolvedBackground = Color(0xFF0D3318),
    badgeResolvedText = Color(0xFF6BCF8B),
    chartGridLine = Color(0xFF3A3A3A),
    resolvedCardAlpha = 0.4f,
)

val LocalWisdometerColors = staticCompositionLocalOf { lightWisdometerColors() }
