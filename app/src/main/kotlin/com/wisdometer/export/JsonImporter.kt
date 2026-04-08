package com.wisdometer.export

import android.content.Context
import android.net.Uri
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PredictionRepository,
    private val converter: JsonConverter,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Returns count of predictions in the file (attempted to import). */
    suspend fun importFromUri(uri: Uri): Int {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("Cannot open input stream for URI: $uri")

        val exportFile = json.decodeFromString(ExportFile.serializer(), jsonString)
        val imported = converter.fromExportFile(exportFile)
        repository.importPredictions(imported)
        return imported.size
    }
}
