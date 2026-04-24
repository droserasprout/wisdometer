package com.wisdometer.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wisdometer.ui.theme.Dim
import com.wisdometer.ui.theme.WisdometerTypography
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportToUri(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importFromUri(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dim.md),
    ) {
        SectionHeader("Notifications")
        Spacer(modifier = Modifier.height(Dim.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Enable notifications", style = WisdometerTypography.bodyMedium)
            Switch(checked = state.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
        }

        Spacer(modifier = Modifier.height(Dim.lg))
        SectionHeader("Data")
        Spacer(modifier = Modifier.height(Dim.sm))

        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        OutlinedButton(
            onClick = { exportLauncher.launch("wisdometer-export-$today.json") },
            modifier = Modifier.fillMaxWidth(),
            shape = Dim.ButtonShape,
        ) {
            Text("Export JSON")
        }
        Spacer(modifier = Modifier.height(Dim.sm))
        OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth(),
            shape = Dim.ButtonShape,
        ) {
            Text("Import JSON")
        }

        state.statusMessage?.let { msg ->
            Spacer(modifier = Modifier.height(Dim.md))
            Text(msg, style = WisdometerTypography.bodySmall, color = MaterialTheme.colorScheme.primary)
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearStatusMessage()
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
