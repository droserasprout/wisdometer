package com.wisdometer.domain

import com.wisdometer.data.model.PredictionWithOptions
import com.wisdometer.data.model.tagList
import javax.inject.Inject
import javax.inject.Singleton

/** One point on a calibration curve: bucket midpoint, actual hit rate, and sample count. */
data class CalibrationPoint(
    val predictedPct: Int,  // bucket midpoint e.g. 5, 15, …, 95
    val actualRate: Double, // fraction of options in this bucket that actually occurred
    val count: Int,
)

@Singleton
class ScoringEngine @Inject constructor() {

    /** Score = probability_of_actual_outcome / 100, averaged across resolved predictions. */
    fun simpleCloseness(resolved: List<PredictionWithOptions>): Double {
        if (resolved.isEmpty()) return 0.0
        return resolved.map { item ->
            val actual = item.actualOption ?: return@map 0.0
            actual.probability / 100.0
        }.average()
    }

    /** Brier score = (p_actual - 1)² + Σ(p_other)², averaged across resolved predictions.
     *  0.0 = perfect, 2.0 = worst. */
    fun brierScore(resolved: List<PredictionWithOptions>): Double {
        if (resolved.isEmpty()) return 0.0
        return resolved.map { item ->
            val actual = item.actualOption ?: return@map 2.0
            val pActual = actual.probability / 100.0
            val sumOthersSq = item.options
                .filter { it.id != actual.id }
                .sumOf { (it.probability / 100.0).let { p -> p * p } }
            (pActual - 1.0).let { it * it } + sumOthersSq
        }.average()
    }

    fun simpleClosenessForTag(items: List<PredictionWithOptions>, tag: String): Double {
        val resolved = items.filter { it.isResolved && tag in it.prediction.tagList }
        return simpleCloseness(resolved)
    }

    fun brierScoreForTag(items: List<PredictionWithOptions>, tag: String): Double {
        val resolved = items.filter { it.isResolved && tag in it.prediction.tagList }
        return brierScore(resolved)
    }

    /** Mean probability of the top-ranked option across ALL predictions (resolved or not). */
    fun avgConfidence(items: List<PredictionWithOptions>): Double {
        if (items.isEmpty()) return 0.0
        return items.map { item ->
            item.options.maxOfOrNull { it.probability }?.toDouble() ?: 0.0
        }.average()
    }

    /** Returns list of (resolvedAtEpochMs, cumulativeAccuracy) sorted by resolution time. */
    fun accuracyOverTime(resolved: List<PredictionWithOptions>): List<Pair<Long, Double>> {
        val sorted = resolved
            .filter { it.isResolved }
            .sortedBy { it.prediction.resolvedAt!!.toEpochMilli() }
        return cumulativeAccuracy(sorted).mapIndexed { i, acc ->
            Pair(sorted[i].prediction.resolvedAt!!.toEpochMilli(), acc)
        }
    }

    /** Returns list of (count, cumulativeAccuracy) sorted by resolution time. */
    fun accuracyOverCount(resolved: List<PredictionWithOptions>): List<Pair<Int, Double>> {
        val sorted = resolved
            .filter { it.isResolved }
            .sortedBy { it.prediction.resolvedAt!!.toEpochMilli() }
        return cumulativeAccuracy(sorted).mapIndexed { i, acc -> Pair(i + 1, acc) }
    }

    /**
     * Calibration: for each 10%-wide probability bucket, what fraction of options in that
     * bucket actually occurred? A well-calibrated forecaster's line tracks the diagonal.
     */
    fun calibrationData(resolved: List<PredictionWithOptions>): List<CalibrationPoint> {
        val buckets = Array(10) { mutableListOf<Boolean>() }
        for (item in resolved) {
            for (option in item.options) {
                val b = (option.probability / 10).coerceIn(0, 9)
                buckets[b].add(option.id == item.prediction.outcomeOptionId)
            }
        }
        return buckets.mapIndexed { i, list ->
            if (list.isEmpty()) null
            else CalibrationPoint(i * 10 + 5, list.count { it }.toDouble() / list.size, list.size)
        }.filterNotNull()
    }

    /**
     * For each 10%-wide bucket, how many predictions had their TOP option in that range?
     * Returns list of (bucketMidpoint, count).
     */
    fun confidenceDistribution(all: List<PredictionWithOptions>): List<Pair<Int, Int>> {
        val counts = IntArray(10)
        for (item in all) {
            val top = item.options.maxOfOrNull { it.probability } ?: continue
            counts[(top / 10).coerceIn(0, 9)]++
        }
        return counts.mapIndexed { i, n -> (i * 10 + 5) to n }
    }

    private fun cumulativeAccuracy(sortedResolved: List<PredictionWithOptions>): List<Double> {
        var runningSum = 0.0
        return sortedResolved.mapIndexed { i, item ->
            val score = item.actualOption?.probability?.div(100.0) ?: 0.0
            runningSum += score
            runningSum / (i + 1)
        }
    }
}
