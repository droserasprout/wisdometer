package com.wisdometer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val WisdometerTypography = Typography(
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryText),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = PrimaryText),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = PrimaryText),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = SecondaryText),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SecondaryText),
)
