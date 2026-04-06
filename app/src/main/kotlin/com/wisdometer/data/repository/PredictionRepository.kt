package com.wisdometer.data.repository

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import kotlinx.coroutines.flow.Flow

interface PredictionRepository {
    fun getAllPredictions(): Flow<List<PredictionWithOptions>>
    fun getPredictionById(id: Long): Flow<PredictionWithOptions?>
    suspend fun savePrediction(prediction: Prediction, options: List<PredictionOption>)
    suspend fun deletePrediction(prediction: Prediction)
    suspend fun getAllResolved(): List<PredictionWithOptions>
    suspend fun getAll(): List<PredictionWithOptions>
    suspend fun importPredictions(items: List<PredictionWithOptions>)
}
