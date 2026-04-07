package com.wisdometer.data.repository

import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.notifications.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class PredictionRepositoryImpl @Inject constructor(
    private val dao: PredictionDao,
    private val notificationScheduler: NotificationScheduler,
) : PredictionRepository {

    override fun getAllPredictions(): Flow<List<PredictionWithOptions>> =
        dao.getAllWithOptions(Instant.now().toEpochMilli())

    override fun getPredictionById(id: Long): Flow<PredictionWithOptions?> =
        dao.getWithOptionsById(id)

    override suspend fun savePrediction(prediction: Prediction, options: List<PredictionOption>) {
        val toSave = if (prediction.id != 0L) prediction.copy(updatedAt = Instant.now()) else prediction
        val savedId = dao.upsertPredictionWithOptions(toSave, options)
        val reminder = toSave.reminderAt
        if (reminder != null && toSave.resolvedAt == null) {
            notificationScheduler.schedule(
                predictionId = savedId,
                reminderAtMs = reminder.toEpochMilli(),
                question = toSave.title,
            )
        } else if (toSave.resolvedAt != null) {
            notificationScheduler.cancel(savedId)
        }
    }

    override suspend fun deletePrediction(prediction: Prediction) {
        notificationScheduler.cancel(prediction.id)
        dao.deletePrediction(prediction)
    }

    override suspend fun getAllResolved(): List<PredictionWithOptions> =
        dao.getAllResolvedWithOptions()

    override suspend fun getAll(): List<PredictionWithOptions> =
        getAllPredictions().first()

    override suspend fun importPredictions(items: List<PredictionWithOptions>) {
        for (item in items) {
            val existing = dao.countByQuestionAndCreatedAt(
                item.prediction.title,
                item.prediction.createdAt.toEpochMilli(),
            )
            if (existing == 0) {
                dao.upsertPredictionWithOptions(
                    item.prediction.copy(id = 0),
                    item.options.map { it.copy(id = 0, predictionId = 0) },
                )
            }
        }
    }
}
