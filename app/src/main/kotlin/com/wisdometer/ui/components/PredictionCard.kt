package com.wisdometer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.ui.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault())

@Composable
fun PredictionCard(
    item: PredictionWithOptions,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardPadding = if (compact) 10.dp else 16.dp
    val optionSpacing = if (compact) 2.dp else 4.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.prediction.question,
                    style = WisdometerTypography.titleMedium,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StatusBadge(isResolved = item.isResolved)
            }

            Spacer(modifier = Modifier.height(optionSpacing + 4.dp))

            item.sortedOptions.forEachIndexed { index, option ->
                ProbabilityBar(
                    label = option.label,
                    probability = option.probability,
                    barColor = BarColors[index % BarColors.size],
                    isActualOutcome = option.id == item.prediction.outcomeOptionId,
                    compact = compact,
                )
            }

            val tags = item.prediction.tagList
            val reminder = item.prediction.reminderAt

            if (tags.isNotEmpty() || reminder != null) {
                Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = tags.joinToString(" · "),
                        style = WisdometerTypography.bodySmall,
                    )
                    if (reminder != null) {
                        Text(
                            text = "⏰ ${dateFormatter.format(reminder)}",
                            style = WisdometerTypography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
