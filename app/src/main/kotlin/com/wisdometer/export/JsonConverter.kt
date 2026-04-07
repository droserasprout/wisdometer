package com.wisdometer.export

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonConverter @Inject constructor() {

    fun toExportFile(items: List<PredictionWithOptions>): ExportFile = ExportFile(
        exportedAt = Instant.now().toString(),
        predictions = items.map { item ->
            ExportedPrediction(
                id = item.prediction.id,
                question = item.prediction.title,
                description = item.prediction.description,
                createdAt = item.prediction.createdAt.toString(),
                updatedAt = item.prediction.updatedAt?.toString(),
                reminderAt = item.prediction.reminderAt?.toString(),
                resolvedAt = item.prediction.resolvedAt?.toString(),
                outcomeOptionId = item.prediction.outcomeOptionId,
                tags = if (item.prediction.tags.isBlank()) emptyList()
                       else item.prediction.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                options = item.sortedOptions.map { opt ->
                    ExportedOption(opt.label, opt.probability, opt.sortOrder)
                },
            )
        },
    )

    fun fromExportFile(file: ExportFile): List<PredictionWithOptions> =
        file.predictions.map { ep ->
            val prediction = Prediction(
                id = 0,
                title = ep.question,
                description = ep.description,
                createdAt = Instant.parse(ep.createdAt),
                updatedAt = ep.updatedAt?.let { Instant.parse(it) },
                reminderAt = ep.reminderAt?.let { Instant.parse(it) },
                resolvedAt = ep.resolvedAt?.let { Instant.parse(it) },
                outcomeOptionId = ep.outcomeOptionId,
                tags = ep.tags.joinToString(","),
            )
            val options = ep.options.mapIndexed { i, opt ->
                PredictionOption(
                    id = 0,
                    predictionId = 0,
                    label = opt.label,
                    probability = opt.probability,
                    sortOrder = opt.sortOrder.takeIf { it >= 0 } ?: i,
                )
            }
            PredictionWithOptions(prediction, options)
        }
}
