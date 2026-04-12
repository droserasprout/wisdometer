package com.wisdometer.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.theme.WisdometerTypography
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun formatDate(millis: Long): String =
    java.time.ZonedDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(millis), ZoneId.systemDefault()
    ).format(dateFmt)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPredictionScreen(
    predictionId: Long?,
    onDone: () -> Unit,
    viewModel: EditPredictionViewModel = hiltViewModel(),
) {
    LaunchedEffect(predictionId) {
        predictionId?.let { viewModel.loadPrediction(it) }
    }

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val canSave = state.question.isNotBlank() &&
        state.reminderAt != null && !state.isSaving

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDone) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(
                    if (predictionId == null) "New Prediction" else "Edit Prediction",
                    style = WisdometerTypography.headlineMedium,
                )
                IconButton(
                    onClick = { viewModel.save(onDone) },
                    enabled = canSave,
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            OutlinedTextField(
                value = state.question,
                onValueChange = viewModel::setQuestion,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Options", style = WisdometerTypography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val weightTotal = state.options.sumOf { it.weight }.toDouble()

            state.options.forEachIndexed { index, option ->
                val pct = if (weightTotal > 0) Math.round(option.weight / weightTotal * 100).toInt() else 0
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = option.label,
                            onValueChange = { viewModel.setOptionLabel(index, it) },
                            label = { Text("Label") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        if (state.options.size > 2) {
                            IconButton(onClick = { viewModel.removeOption(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove option")
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Slider(
                            value = option.weight.toFloat(),
                            onValueChange = { viewModel.setOptionWeight(index, Math.round(it)) },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${option.weight}.0 ($pct%)",
                            style = WisdometerTypography.bodySmall,
                            modifier = Modifier.width(80.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(onClick = viewModel::addOption) {
                Text("+ Add option")
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.tagsInput,
                onValueChange = viewModel::setTagsInput,
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Start date
            val startLabel = formatDate(state.createdAt.toEpochMilli())
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Start: $startLabel")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // End date (required)
            val endLabel = state.reminderAt?.let { "End: ${formatDate(it.toEpochMilli())}" }
                ?: "Set end date"
            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = if (state.reminderAt == null)
                    ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                else
                    ButtonDefaults.outlinedButtonColors(),
            ) {
                Text(endLabel)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start date picker dialog
            if (showStartDatePicker) {
                val pickerState = rememberDatePickerState(
                    initialSelectedDateMillis = state.createdAt.toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showStartDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            pickerState.selectedDateMillis?.let {
                                viewModel.setStartDate(java.time.Instant.ofEpochMilli(it))
                            }
                            showStartDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                    },
                ) {
                    DatePicker(state = pickerState)
                }
            }

            // End date picker dialog
            if (showEndDatePicker) {
                val pickerState = rememberDatePickerState(
                    initialSelectedDateMillis = state.reminderAt?.toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showEndDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            pickerState.selectedDateMillis?.let {
                                viewModel.setEndDate(java.time.Instant.ofEpochMilli(it))
                            }
                            showEndDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
                    },
                ) {
                    DatePicker(state = pickerState)
                }
            }
        }
    }
}
