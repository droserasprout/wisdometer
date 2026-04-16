package com.wisdometer.ui.edit

import androidx.compose.foundation.layout.*
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
import com.wisdometer.ui.components.WeightInputBar
import com.wisdometer.ui.theme.Dim
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
    var showDiscardDialog by remember { mutableStateOf(false) }

    val questionError = state.showValidation && state.question.isBlank()
    val endDateError = state.showValidation && state.reminderAt == null

    fun attemptClose() {
        if (state.isDirty) showDiscardDialog = true else onDone()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = ::attemptClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(
                    if (predictionId == null) "New Prediction" else "Edit Prediction",
                    style = WisdometerTypography.headlineMedium,
                )
                IconButton(
                    onClick = { viewModel.save(onDone) },
                    enabled = !state.isSaving,
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = Dim.md)
                .verticalScroll(scrollState),
        ) {
            OutlinedTextField(
                value = state.question,
                onValueChange = viewModel::setQuestion,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = Dim.ButtonShape,
                isError = questionError,
                supportingText = if (questionError) {
                    { Text("Required") }
                } else null,
            )
            Spacer(modifier = Modifier.height(Dim.sm))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = Dim.ButtonShape,
            )
            Spacer(modifier = Modifier.height(Dim.md))

            Text("Options", style = WisdometerTypography.titleMedium)
            Spacer(modifier = Modifier.height(Dim.sm))

            state.options.forEachIndexed { index, option ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dim.sm),
                    ) {
                        OutlinedTextField(
                            value = option.label,
                            onValueChange = { viewModel.setOptionLabel(index, it) },
                            label = { Text("Label") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = Dim.ButtonShape,
                        )
                        if (state.options.size > 2) {
                            IconButton(onClick = { viewModel.removeOption(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove option")
                            }
                        }
                    }
                    WeightInputBar(
                        weight = option.weight,
                        onWeightChange = { viewModel.setOptionWeight(index, it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(modifier = Modifier.height(Dim.sm))
            }

            TextButton(onClick = viewModel::addOption) {
                Text("+ Add option")
            }

            Spacer(modifier = Modifier.height(Dim.md))
            OutlinedTextField(
                value = state.tagsInput,
                onValueChange = viewModel::setTagsInput,
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = Dim.ButtonShape,
            )

            Spacer(modifier = Modifier.height(Dim.md))

            val startLabel = formatDate(state.createdAt.toEpochMilli())
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = Dim.ButtonShape,
            ) {
                Text("Start: $startLabel")
            }

            Spacer(modifier = Modifier.height(Dim.sm))

            val endLabel = state.reminderAt?.let { "End: ${formatDate(it.toEpochMilli())}" }
                ?: "Set end date"
            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = Dim.ButtonShape,
            ) {
                Text(endLabel)
            }
            if (endDateError) {
                Text(
                    "Required",
                    style = WisdometerTypography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = Dim.md, top = Dim.xs),
                )
            }

            Spacer(modifier = Modifier.height(Dim.lg))

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

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("Unsaved edits will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDone()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            },
        )
    }
}
