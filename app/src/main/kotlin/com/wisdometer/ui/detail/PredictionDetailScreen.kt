package com.wisdometer.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.wisdometer.data.model.tagList
import com.wisdometer.ui.components.ProbabilityBar
import com.wisdometer.ui.components.StatusBadge
import com.wisdometer.share.ShareImageRenderer
import com.wisdometer.ui.theme.WisdometerTypography
import com.wisdometer.ui.theme.weightColor

private val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault())

@Composable
fun PredictionDetailScreen(
    predictionId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: PredictionDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(predictionId) { viewModel.load(predictionId) }

    val item by viewModel.item.collectAsState()
    var showOutcomeDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(1f))
                item?.let { pw ->
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        ShareImageRenderer.sharePredictionCard(
                            context = ctx,
                            question = pw.prediction.title,
                            options = pw.sortedOptions.map { it.label to it.weight },
                            isResolved = pw.isResolved,
                            actualOptionLabel = pw.actualOption?.label,
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        },
    ) { padding ->
        item?.let { pw ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pw.prediction.title,
                        style = WisdometerTypography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(isResolved = pw.isResolved)
                }
                if (pw.prediction.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(pw.prediction.description, style = WisdometerTypography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            dateFmt.format(pw.prediction.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    pw.prediction.updatedAt?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                dateFmt.format(it),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                val pctMap = pw.normalizedPercentages
                val topOptionId = pw.sortedOptions.maxByOrNull { it.weight }?.id
                pw.sortedOptions.forEachIndexed { index, option ->
                    ProbabilityBar(
                        label = option.label,
                        probability = pctMap[option.id] ?: 0,
                        weight = option.weight,
                        barColor = weightColor(option.weight),
                        isActualOutcome = option.id == pw.prediction.outcomeOptionId,
                        isTopPrediction = option.id == topOptionId,
                        compact = false,
                    )
                }

                val tags = pw.prediction.tagList
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tags: ${tags.joinToString(", ")}", style = WisdometerTypography.bodySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!pw.isResolved) {
                    Button(
                        onClick = { showOutcomeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Set Outcome")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Delete Prediction")
                }
            }

            if (showOutcomeDialog) {
                AlertDialog(
                    onDismissRequest = { showOutcomeDialog = false },
                    title = { Text("Select Outcome") },
                    text = {
                        Column {
                            pw.sortedOptions.forEach { option ->
                                TextButton(
                                    onClick = {
                                        viewModel.setOutcome(option.id)
                                        showOutcomeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(option.label)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showOutcomeDialog = false }) { Text("Cancel") }
                    },
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Prediction?") },
                    text = { Text("This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    },
                )
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
