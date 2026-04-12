package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wisdometer.ui.theme.WisdometerTypography
import com.wisdometer.ui.theme.weightColor

@Composable
fun ProbabilityBar(
    label: String,
    probability: Int,
    weight: Int,
    barColor: Color,
    isActualOutcome: Boolean,
    isTopPrediction: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val barHeight: Dp = if (compact) 6.dp else 10.dp
    val verticalPadding: Dp = if (compact) 2.dp else 4.dp
    val gap: Dp = if (compact) 2.dp else 3.dp

    val prefix = when {
        isActualOutcome && isTopPrediction -> "🎯🔮 "
        isActualOutcome -> "🎯 "
        isTopPrediction -> "🔮 "
        else -> ""
    }

    val textStyle: TextStyle = if (compact) WisdometerTypography.labelSmall else WisdometerTypography.bodyLarge

    Column(modifier = modifier.padding(vertical = verticalPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$prefix$label",
                style = textStyle,
                color = if (isActualOutcome) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${weight}.0",
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(barHeight),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            for (i in 1..10) {
                val segmentColor = if (i <= weight) weightColor(i) else weightColor(i).copy(alpha = 0.15f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(segmentColor),
                )
            }
        }
    }
}
