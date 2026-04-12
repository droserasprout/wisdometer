package com.wisdometer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import com.wisdometer.ui.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault())
private val dateFmtShort = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())

@Composable
fun PredictionCard(
    item: PredictionWithOptions,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardPadding = if (compact) 10.dp else 16.dp
    val optionSpacing = if (compact) 2.dp else 4.dp
    val wisdometerColors = LocalWisdometerColors.current
    val resolvedAlpha = if (item.isResolved) wisdometerColors.resolvedCardAlpha else 1f
    val pctMap = item.normalizedPercentages
    val topOptionId = item.sortedOptions.maxByOrNull { it.weight }?.id
    val tags = item.prediction.tagList
    val reminder = item.prediction.reminderAt

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(resolvedAlpha)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            // Title + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.prediction.title,
                    style = WisdometerTypography.titleMedium,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StatusBadge(isResolved = item.isResolved)
            }

            // Description
            if (item.prediction.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.prediction.description,
                    style = WisdometerTypography.bodySmall,
                    maxLines = if (compact) 1 else Int.MAX_VALUE,
                )
            }

            Spacer(modifier = Modifier.height(optionSpacing + 4.dp))

            item.sortedOptions.forEachIndexed { index, option ->
                ProbabilityBar(
                    label = option.label,
                    probability = pctMap[option.id] ?: 0,
                    weight = option.weight,
                    barColor = weightColor(option.weight),
                    isActualOutcome = option.id == item.prediction.outcomeOptionId,
                    isTopPrediction = option.id == topOptionId,
                    compact = compact,
                )
            }

            // Tags + reminder row
            if (tags.isNotEmpty() || reminder != null) {
                Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (tags.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Label,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = tags.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (reminder != null) {
                        Text(
                            text = "⏰ ${dateFmt.format(reminder)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Dates row (created + updated)
            if (!compact) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "📅 ${dateFmtShort.format(item.prediction.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    item.prediction.updatedAt?.let { updatedAt ->
                        Text(
                            text = "✏️ ${dateFmtShort.format(updatedAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
