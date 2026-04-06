package com.wisdometer.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wisdometer.ui.theme.WisdometerTypography
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
            .padding(16.dp),
    ) {
        Text("Settings", style = WisdometerTypography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        // Compact mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Compact mode", style = WisdometerTypography.bodyMedium)
            Switch(checked = state.compact, onCheckedChange = viewModel::setCompact)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Notifications toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Notifications", style = WisdometerTypography.bodyMedium)
            Switch(checked = state.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Export JSON button
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        OutlinedButton(
            onClick = { exportLauncher.launch("wisdometer-export-$today.json") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export JSON")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Import JSON button
        OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Import JSON")
        }

        // Status message (auto-dismiss after 3 seconds)
        state.statusMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(msg, style = WisdometerTypography.bodySmall, color = MaterialTheme.colorScheme.primary)
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearStatusMessage()
            }
        }
    }
}
