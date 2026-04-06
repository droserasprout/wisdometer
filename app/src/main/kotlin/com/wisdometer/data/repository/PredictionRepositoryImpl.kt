package com.wisdometer.data.repository

import com.wisdometer.data.dao.PredictionDao
import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class PredictionRepositoryImpl @Inject constructor(
    private val dao: PredictionDao,
) : PredictionRepository {

    override fun getAllPredictions(): Flow<List<PredictionWithOptions>> =
        dao.getAllWithOptions(Instant.now().toEpochMilli())

    override fun getPredictionById(id: Long): Flow<PredictionWithOptions?> =
        dao.getWithOptionsById(id)

    override suspend fun savePrediction(prediction: Prediction, options: List<PredictionOption>) {
        dao.upsertPredictionWithOptions(prediction, options)
    }

    override suspend fun deletePrediction(prediction: Prediction) {
        dao.deletePrediction(prediction)
    }

    override suspend fun getAllResolved(): List<PredictionWithOptions> =
        dao.getAllResolvedWithOptions()

    override suspend fun getAll(): List<PredictionWithOptions> =
        getAllPredictions().first()

    override suspend fun importPredictions(items: List<PredictionWithOptions>) {
        for (item in items) {
            val existing = dao.countByQuestionAndCreatedAt(
                item.prediction.question,
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
