package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.LocalWisdometerColors

@Composable
fun StatusBadge(isResolved: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalWisdometerColors.current
    val bg = if (isResolved) colors.badgeResolvedBackground else colors.badgeOpenBackground
    val text = if (isResolved) colors.badgeResolvedText else colors.badgeOpenText
    val label = if (isResolved) "RESOLVED" else "OPEN"

    Text(
        text = label,
        fontSize = 10.sp,
        color = text,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
