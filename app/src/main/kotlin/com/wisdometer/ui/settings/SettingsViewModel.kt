package com.wisdometer.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisdometer.export.JsonExporter
import com.wisdometer.export.JsonImporter
import com.wisdometer.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PREFS_NAME = "wisdometer_settings"
private const val KEY_COMPACT = "compact_mode"
private const val KEY_NOTIFICATIONS = "notifications_enabled"

data class SettingsUiState(
    val compact: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val statusMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exporter: JsonExporter,
    private val importer: JsonImporter,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        SettingsUiState(
            compact = prefs.getBoolean(KEY_COMPACT, false),
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true),
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setCompact(value: Boolean) {
        prefs.edit().putBoolean(KEY_COMPACT, value).apply()
        _state.update { it.copy(compact = value) }
    }

    fun setNotificationsEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()
        _state.update { it.copy(notificationsEnabled = value) }
        if (!value) notificationScheduler.cancelAll()
    }

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                exporter.exportToUri(uri)
                _state.update { it.copy(statusMessage = "Export successful") }
            } catch (e: Exception) {
                _state.update { it.copy(statusMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val count = importer.importFromUri(uri)
                _state.update { it.copy(statusMessage = "Imported $count predictions") }
            } catch (e: Exception) {
                _state.update { it.copy(statusMessage = "Import failed: ${e.message}") }
            }
        }
    }

    fun clearStatusMessage() = _state.update { it.copy(statusMessage = null) }
}
