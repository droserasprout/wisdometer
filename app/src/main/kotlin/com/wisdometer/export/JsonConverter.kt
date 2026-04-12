package com.wisdometer.export

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Imported prediction with optional outcome index (used to remap outcomeOptionId after insert). */
data class ImportedPrediction(
    val item: PredictionWithOptions,
    val outcomeOptionIndex: Int? = null,
)

@Singleton
class JsonConverter @Inject constructor() {

    fun toExportFile(items: List<PredictionWithOptions>): ExportFile = ExportFile(
        exportedAt = Instant.now().toString(),
        predictions = items.map { item ->
            val outcomeIndex = item.prediction.outcomeOptionId?.let { outcomeId ->
                item.sortedOptions.indexOfFirst { it.id == outcomeId }.takeIf { it >= 0 }
            }
            ExportedPrediction(
                id = item.prediction.id,
                question = item.prediction.title,
                description = item.prediction.description,
                createdAt = item.prediction.createdAt.toString(),
                updatedAt = item.prediction.updatedAt?.toString(),
                reminderAt = item.prediction.reminderAt?.toString(),
                resolvedAt = item.prediction.resolvedAt?.toString(),
                outcomeOptionIndex = outcomeIndex,
                tags = if (item.prediction.tags.isBlank()) emptyList()
                       else item.prediction.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                options = run {
                    val pcts = item.normalizedPercentages
                    item.sortedOptions.map { opt ->
                        ExportedOption(opt.label, opt.weight, pcts[opt.id] ?: 0, opt.sortOrder)
                    }
                },
            )
        },
    )

    fun fromExportFile(file: ExportFile): List<ImportedPrediction> =
        file.predictions.map { ep ->
            val prediction = Prediction(
                id = 0,
                title = ep.question,
                description = ep.description,
                createdAt = Instant.parse(ep.createdAt),
                updatedAt = ep.updatedAt?.let { Instant.parse(it) },
                reminderAt = ep.reminderAt?.let { Instant.parse(it) },
                resolvedAt = ep.resolvedAt?.let { Instant.parse(it) },
                tags = ep.tags.joinToString(","),
            )
            val options = ep.options.mapIndexed { i, opt ->
                PredictionOption(
                    id = 0,
                    predictionId = 0,
                    label = opt.label,
                    weight = opt.weight,
                    sortOrder = opt.sortOrder.takeIf { it >= 0 } ?: i,
                )
            }
            ImportedPrediction(
                item = PredictionWithOptions(prediction, options),
                outcomeOptionIndex = ep.outcomeOptionIndex,
            )
        }
}
