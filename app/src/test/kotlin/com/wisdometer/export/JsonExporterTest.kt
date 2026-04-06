package com.wisdometer.export

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class JsonExporterTest {

    private val converter = JsonConverter()

    @Test
    fun `toExportFile converts predictions with tags and options`() {
        val prediction = Prediction(
            id = 1L, question = "Will I find a job?", tags = "career,finance",
            outcomeOptionId = 2L, resolvedAt = Instant.parse("2026-06-01T00:00:00Z"),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        val options = listOf(
            PredictionOption(id = 1L, predictionId = 1L, label = "No", probability = 60, sortOrder = 0),
            PredictionOption(id = 2L, predictionId = 1L, label = "Yes", probability = 40, sortOrder = 1),
        )
        val exportFile = converter.toExportFile(listOf(PredictionWithOptions(prediction, options)))

        assertEquals(1, exportFile.version)
        assertEquals(1, exportFile.predictions.size)
        val ep = exportFile.predictions[0]
        assertEquals("Will I find a job?", ep.question)
        assertEquals(listOf("career", "finance"), ep.tags)
        assertEquals(2L, ep.outcomeOptionId)
        assertEquals("2026-06-01T00:00:00Z", ep.resolvedAt)
        assertEquals(2, ep.options.size)
        assertEquals("No", ep.options[0].label)
        assertEquals(60, ep.options[0].probability)
    }

    @Test
    fun `toExportFile with empty tags produces empty list`() {
        val prediction = Prediction(id = 1L, question = "Q", tags = "", createdAt = Instant.EPOCH)
        val opt = PredictionOption(id = 1L, predictionId = 1L, label = "Y", probability = 100, sortOrder = 0)
        val file = converter.toExportFile(listOf(PredictionWithOptions(prediction, listOf(opt))))
        assertEquals(emptyList<String>(), file.predictions[0].tags)
    }

    @Test
    fun `JSON round-trip preserves all fields`() {
        val prediction = Prediction(id = 1L, question = "Round trip?", tags = "test", createdAt = Instant.EPOCH)
        val opt = PredictionOption(id = 1L, predictionId = 1L, label = "Yes", probability = 100, sortOrder = 0)
        val original = converter.toExportFile(listOf(PredictionWithOptions(prediction, listOf(opt))))
        val json = Json.encodeToString(ExportFile.serializer(), original)
        val decoded = Json.decodeFromString(ExportFile.serializer(), json)
        assertEquals(original, decoded)
    }
}
