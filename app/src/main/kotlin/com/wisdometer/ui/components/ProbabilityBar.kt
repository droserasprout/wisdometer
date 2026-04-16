package com.wisdometer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wisdometer.ui.theme.Dim
import com.wisdometer.ui.theme.WisdometerTypography
import com.wisdometer.ui.theme.weightColor

@Composable
fun ProbabilityBar(
    label: String,
    weight: Int,
    barColor: Color,
    isActualOutcome: Boolean,
    isTopPrediction: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = Dim.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (isActualOutcome) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = "actual outcome",
                        modifier = Modifier.size(14.dp),
                        tint = barColor,
                    )
                }
                if (isTopPrediction) {
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = "top prediction",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = label,
                    style = WisdometerTypography.bodyLarge,
                    color = if (isActualOutcome) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "$weight",
                style = WisdometerTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(10.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (i in 1..10) {
                val segmentColor = if (i <= weight) weightColor(i) else weightColor(i).copy(alpha = 0.15f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(Dim.BarShape)
                        .background(segmentColor),
                )
            }
        }
    }
}
