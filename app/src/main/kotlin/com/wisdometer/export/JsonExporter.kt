package com.wisdometer.export

import android.content.Context
import android.net.Uri
import com.wisdometer.data.repository.PredictionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PredictionRepository,
    private val converter: JsonConverter,
) {
    private val json = Json { prettyPrint = true }

    suspend fun exportToUri(uri: Uri) {
        val all = repository.getAll()
        val exportFile = converter.toExportFile(all)
        val jsonString = json.encodeToString(ExportFile.serializer(), exportFile)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonString.toByteArray(Charsets.UTF_8))
        } ?: error("Cannot open output stream for URI: $uri")
    }
}
