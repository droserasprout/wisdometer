package com.wisdometer.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.share.ShareImageRenderer
import com.wisdometer.ui.theme.BarColors
import com.wisdometer.ui.theme.WisdometerTypography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val chartDateFormatter = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var useTimeAxis by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Profile", style = WisdometerTypography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Accuracy card with donut ring
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AccuracyDonut(
                    fraction = state.simpleCloseness.toFloat(),
                    modifier = Modifier.size(72.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        if (state.isLoaded) "${(state.simpleCloseness * 100).roundToInt()}% Accuracy"
                        else "... Accuracy",
                        style = WisdometerTypography.headlineLarge,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            if (state.isLoaded) "Brier: ${"%.2f".format(state.brierScore)}" else "Brier: ...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        var showTooltip by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(0.dp),
                        ) {
                            TextButton(
                                onClick = { showTooltip = !showTooltip },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    "?",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            if (showTooltip) {
                                AlertDialog(
                                    onDismissRequest = { showTooltip = false },
                                    title = { Text("Brier Score") },
                                    text = { Text("Measures calibration — 0.0 is perfect, 2.0 is worst.") },
                                    confirmButton = {
                                        TextButton(onClick = { showTooltip = false }) { Text("OK") }
                                    },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (state.isLoaded)
                            "${state.resolvedPredictions} resolved · ${state.openPredictions} open"
                        else "... resolved · ... open",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2×2 stat tiles
        val statTiles = listOf(
            Triple("Total", if (state.isLoaded) state.totalPredictions.toString() else "...", BarColors[0]),
            Triple("Resolved", if (state.isLoaded) state.resolvedPredictions.toString() else "...", BarColors[1]),
            Triple("Open", if (state.isLoaded) state.openPredictions.toString() else "...", BarColors[2]),
            Triple("Avg Confidence", if (state.isLoaded) "${"%.1f".format(state.avgConfidence)}" else "...", BarColors[3]),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            statTiles.chunked(2).forEach { rowTiles ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowTiles.forEach { (label, value, accentColor) ->
                        StatTile(
                            label = label,
                            value = value,
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accuracy chart card with toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Accuracy over time", style = WisdometerTypography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = useTimeAxis,
                        onClick = { useTimeAxis = true },
                        label = { Text("Over time") },
                    )
                    FilterChip(
                        selected = !useTimeAxis,
                        onClick = { useTimeAxis = false },
                        label = { Text("By count") },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val chartPoints = if (useTimeAxis) state.accuracyOverTime else state.accuracyOverCount
                val xFirst = chartPoints.firstOrNull()?.first?.let { v ->
                    if (useTimeAxis) chartDateFormatter.format(Instant.ofEpochMilli(v as Long))
                    else "#$v"
                } ?: ""
                val xLast = chartPoints.lastOrNull()?.first?.let { v ->
                    if (useTimeAxis) chartDateFormatter.format(Instant.ofEpochMilli(v as Long))
                    else "#$v"
                } ?: ""
                AccuracyChart(
                    points = chartPoints,
                    xLabelFirst = xFirst,
                    xLabelLast = xLast,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }
        }

        // Calibration chart
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Calibration", style = WisdometerTypography.titleMedium)
                Text(
                    "Predicted % vs actual hit rate · dashed = perfect",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                CalibrationChart(
                    points = state.calibration,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            }
        }

        // Confidence distribution
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Confidence distribution", style = WisdometerTypography.titleMedium)
                Text(
                    "How many options you've assigned each weight (bars) and how many turned out correct (ticks)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ConfidenceChart(
                    distribution = state.confidenceDistribution,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                )
            }
        }

        // Tag accuracy breakdown
        if (state.tagAccuracies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("By Tag", style = WisdometerTypography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    state.tagAccuracies.forEach { ta ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(ta.tag, style = WisdometerTypography.bodyMedium)
                            Text(
                                "${(ta.closeness * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val ctx = androidx.compose.ui.platform.LocalContext.current
        OutlinedButton(
            shape = RoundedCornerShape(8.dp),
            onClick = {
                ShareImageRenderer.shareProfileStats(
                    context = ctx,
                    accuracy = (state.simpleCloseness * 100).roundToInt(),
                    brierScore = state.brierScore,
                    totalPredictions = state.totalPredictions,
                    resolvedPredictions = state.resolvedPredictions,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp),
            )
            Text("Share Stats")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Colored top border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .then(Modifier.wrapContentSize(Alignment.TopStart)),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = accentColor)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    value,
                    style = WisdometerTypography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AccuracyDonut(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = BarColors[0]
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.15f
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)
        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        // Fill
        drawArc(
            color = fillColor,
            startAngle = -90f,
            sweepAngle = 360f * fraction.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}
