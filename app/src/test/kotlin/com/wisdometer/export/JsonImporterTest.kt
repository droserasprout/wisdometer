package com.wisdometer.export

import com.wisdometer.data.model.PredictionWithOptions
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class JsonImporterTest {

    private val converter = JsonConverter()

    @Test
    fun `fromExportFile converts options and tags correctly`() {
        val exportFile = ExportFile(
            version = 1,
            exportedAt = "2026-04-06T12:00:00Z",
            predictions = listOf(
                ExportedPrediction(
                    id = 42L, question = "Test?",
                    createdAt = "2026-01-01T00:00:00Z",
                    tags = listOf("a", "b"),
                    options = listOf(
                        ExportedOption("Yes", 70, 0),
                        ExportedOption("No", 30, 1),
                    ),
                )
            ),
        )
        val imported = converter.fromExportFile(exportFile)
        assertEquals(1, imported.size)
        val item = imported[0].item
        assertEquals("Test?", item.prediction.title)
        assertEquals("a,b", item.prediction.tags)
        assertNull(item.prediction.resolvedAt)
        assertEquals(2, item.options.size)
        assertEquals("Yes", item.options[0].label)
        assertEquals(70, item.options[0].probability)
    }

    @Test
    fun `fromExportFile preserves resolved_at and outcome index`() {
        val exportFile = ExportFile(
            version = 1,
            exportedAt = "2026-04-06T12:00:00Z",
            predictions = listOf(
                ExportedPrediction(
                    id = 1L, question = "Q",
                    createdAt = "2026-01-01T00:00:00Z",
                    resolvedAt = "2026-03-01T00:00:00Z",
                    outcomeOptionIndex = 0,
                    tags = emptyList(),
                    options = listOf(ExportedOption("Yes", 100, 0)),
                )
            ),
        )
        val imported = converter.fromExportFile(exportFile)
        assertNotNull(imported[0].item.prediction.resolvedAt)
        assertNull(imported[0].item.prediction.outcomeOptionId) // resolved by repository after insert
        assertEquals(0, imported[0].outcomeOptionIndex)
    }

    @Test
    fun `fromExportFile resets IDs to 0 for new import`() {
        val exportFile = ExportFile(
            version = 1,
            exportedAt = "2026-04-06T12:00:00Z",
            predictions = listOf(
                ExportedPrediction(
                    id = 99L, question = "Q",
                    createdAt = "2026-01-01T00:00:00Z",
                    tags = emptyList(),
                    options = listOf(ExportedOption("Yes", 100, 0)),
                )
            ),
        )
        val imported = converter.fromExportFile(exportFile)
        assertEquals(0L, imported[0].item.prediction.id)
        assertEquals(0L, imported[0].item.options[0].id)
        assertEquals(0L, imported[0].item.options[0].predictionId)
    }
}
