package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wisdometer.ui.theme.LocalWisdometerColors
import com.wisdometer.ui.theme.WisdometerTypography

@Composable
fun StatusBadge(isResolved: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalWisdometerColors.current
    val bg = if (isResolved) colors.badgeResolvedBackground else colors.badgeOpenBackground
    val text = if (isResolved) colors.badgeResolvedText else colors.badgeOpenText
    val label = if (isResolved) "RESOLVED" else "OPEN"

    Text(
        text = label,
        style = WisdometerTypography.bodySmall,
        color = text,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
