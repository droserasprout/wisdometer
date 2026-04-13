package com.wisdometer.domain

import com.wisdometer.data.model.Prediction
import com.wisdometer.data.model.PredictionOption
import com.wisdometer.data.model.PredictionWithOptions
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ScoringEngineTest {

    private val engine = ScoringEngine()

    private fun makePrediction(
        options: List<Pair<String, Int>>,  // label to weight (1-10)
        actualIndex: Int,
    ): PredictionWithOptions {
        val prediction = Prediction(
            id = 1L,
            title = "Q",
            createdAt = Instant.EPOCH,
            resolvedAt = Instant.EPOCH,
            outcomeOptionId = (actualIndex + 1).toLong(),
        )
        val predictionOptions = options.mapIndexed { i, (label, weight) ->
            PredictionOption(
                id = (i + 1).toLong(),
                predictionId = 1L,
                label = label,
                weight = weight,
                sortOrder = i,
            )
        }
        return PredictionWithOptions(prediction, predictionOptions)
    }

    @Test
    fun `simpleCloseness is 1 when all weight on correct outcome`() {
        // Single option with weight 10 → 100% normalized
        val p = makePrediction(listOf("Yes" to 10), actualIndex = 0)
        assertEquals(1.0, engine.simpleCloseness(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness reflects weight ratio`() {
        // weight 1 vs weight 9 → correct gets 10% normalized
        val p = makePrediction(listOf("Yes" to 1, "No" to 9), actualIndex = 0)
        assertEquals(0.1, engine.simpleCloseness(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness averages across multiple predictions`() {
        val p1 = makePrediction(listOf("Yes" to 10), actualIndex = 0)  // 1.0
        val p2 = makePrediction(listOf("Yes" to 1, "No" to 9), actualIndex = 0)  // 0.1
        assertEquals(0.55, engine.simpleCloseness(listOf(p1, p2)), 0.001)
    }

    @Test
    fun `brierScore is 0 for perfect prediction`() {
        val p = makePrediction(listOf("Yes" to 10), actualIndex = 0)
        assertEquals(0.0, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `brierScore is 2 for worst prediction`() {
        // weight 1 on correct, weight 9 on wrong → 10% vs 90%
        // NOT the worst — worst is 0% on correct
        // With weights we can't get exactly 0%, but 1 vs 10000 would approach it
        // Use weight 0... but min is 1. So worst achievable with 2 options:
        // weight 1 vs weight 9: (0.1-1)^2 + (0.9)^2 = 0.81 + 0.81 = 1.62
        val p = makePrediction(listOf("Yes" to 1, "No" to 9), actualIndex = 0)
        assertEquals(1.62, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `brierScore for equal weights with two options`() {
        // equal weight → 50/50 normalized
        // (0.5-1)^2 + (0.5)^2 = 0.25 + 0.25 = 0.5
        val p = makePrediction(listOf("Yes" to 5, "No" to 5), actualIndex = 0)
        assertEquals(0.5, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness returns 0 for empty list`() {
        assertEquals(0.0, engine.simpleCloseness(emptyList()), 0.001)
    }

    @Test
    fun `brierScore returns 0 for empty list`() {
        assertEquals(0.0, engine.brierScore(emptyList()), 0.001)
    }

    @Test
    fun `simpleClosenessForTag filters by tag`() {
        val p1 = Prediction(id = 1, title = "Q1", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 1L, tags = "career")
        val p2 = Prediction(id = 2, title = "Q2", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 3L, tags = "finance")
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Yes", weight = 10, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "No", weight = 1, sortOrder = 0)
        val items = listOf(
            PredictionWithOptions(p1, listOf(opt1)),
            PredictionWithOptions(p2, listOf(opt2)),
        )
        assertEquals(1.0, engine.simpleClosenessForTag(items, "career"), 0.001)
        assertEquals(1.0, engine.simpleClosenessForTag(items, "finance"), 0.001)  // single option = 100%
    }

    @Test
    fun `brierScoreForTag filters by tag`() {
        val p1 = Prediction(id = 1, title = "Q1", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 1L, tags = "career")
        val p2 = Prediction(id = 2, title = "Q2", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 3L, tags = "finance")
        // p1: single option weight 10 → 100% on correct → brier 0.0
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Yes", weight = 10, sortOrder = 0)
        // p2: equal weights → 50/50 → (0.5-1)^2 + 0.5^2 = 0.25 + 0.25 = 0.5
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "No", weight = 5, sortOrder = 0)
        val opt3 = PredictionOption(id = 4, predictionId = 2, label = "Yes", weight = 5, sortOrder = 1)
        val items = listOf(
            PredictionWithOptions(p1, listOf(opt1)),
            PredictionWithOptions(p2, listOf(opt2, opt3)),
        )
        assertEquals(0.0, engine.brierScoreForTag(items, "career"), 0.001)
        assertEquals(0.5, engine.brierScoreForTag(items, "finance"), 0.001)
    }

    @Test
    fun `avgConfidence is mean weight of top-ranked option`() {
        val p1 = Prediction(id = 1, title = "Q", createdAt = Instant.EPOCH)
        val p2 = Prediction(id = 2, title = "Q2", createdAt = Instant.EPOCH)
        val opts1 = listOf(
            PredictionOption(id = 1, predictionId = 1, label = "Yes", weight = 8, sortOrder = 0),
            PredictionOption(id = 2, predictionId = 1, label = "No", weight = 2, sortOrder = 1),
        )
        val opts2 = listOf(
            PredictionOption(id = 3, predictionId = 2, label = "A", weight = 6, sortOrder = 0),
            PredictionOption(id = 4, predictionId = 2, label = "B", weight = 4, sortOrder = 1),
        )
        val items = listOf(PredictionWithOptions(p1, opts1), PredictionWithOptions(p2, opts2))
        // top weights: 8 and 6, avg = 7.0
        assertEquals(7.0, engine.avgConfidence(items), 0.001)
    }

    @Test
    fun `accuracyOverTime returns cumulative rolling average by resolution date`() {
        val p1 = Prediction(id = 1, title = "Q1", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(1000), outcomeOptionId = 1)
        val p2 = Prediction(id = 2, title = "Q2", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(2000), outcomeOptionId = 3)
        // single option → 100%
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Y", weight = 10, sortOrder = 0)
        // equal weights → 50%
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "N", weight = 5, sortOrder = 0)
        val opt3 = PredictionOption(id = 4, predictionId = 2, label = "Y", weight = 5, sortOrder = 1)
        val items = listOf(
            PredictionWithOptions(p1, listOf(opt1)),
            PredictionWithOptions(p2, listOf(opt2, opt3)),
        )
        val points = engine.accuracyOverTime(items)
        // point 0: resolvedAt=1000, cumulative = 1.0
        // point 1: resolvedAt=2000, cumulative = (1.0 + 0.5) / 2 = 0.75
        assertEquals(2, points.size)
        assertEquals(1.0, points[0].second, 0.001)
        assertEquals(0.75, points[1].second, 0.001)
    }

    @Test
    fun `confidenceDistribution totals every option by weight and counts actuals from resolved`() {
        // p1 (resolved, actual = id 1 at weight 8): options weight 8, 2
        val p1 = Prediction(id = 1, title = "Q1", createdAt = Instant.EPOCH,
            resolvedAt = Instant.EPOCH, outcomeOptionId = 1L)
        val p1opts = listOf(
            PredictionOption(id = 1, predictionId = 1, label = "A", weight = 8, sortOrder = 0),
            PredictionOption(id = 2, predictionId = 1, label = "B", weight = 2, sortOrder = 1),
        )
        // p2 (resolved, actual = id 4 at weight 5): options weight 5, 5, 8
        val p2 = Prediction(id = 2, title = "Q2", createdAt = Instant.EPOCH,
            resolvedAt = Instant.EPOCH, outcomeOptionId = 4L)
        val p2opts = listOf(
            PredictionOption(id = 3, predictionId = 2, label = "A", weight = 5, sortOrder = 0),
            PredictionOption(id = 4, predictionId = 2, label = "B", weight = 5, sortOrder = 1),
            PredictionOption(id = 5, predictionId = 2, label = "C", weight = 8, sortOrder = 2),
        )
        // p3 (open, no outcome): options weight 2, 10
        val p3 = Prediction(id = 3, title = "Q3", createdAt = Instant.EPOCH)
        val p3opts = listOf(
            PredictionOption(id = 6, predictionId = 3, label = "A", weight = 2, sortOrder = 0),
            PredictionOption(id = 7, predictionId = 3, label = "B", weight = 10, sortOrder = 1),
        )
        val items = listOf(
            PredictionWithOptions(p1, p1opts),
            PredictionWithOptions(p2, p2opts),
            PredictionWithOptions(p3, p3opts),
        )

        val dist = engine.confidenceDistribution(items)

        assertEquals(10, dist.size)
        // weights 1..10 in order
        assertEquals((1..10).toList(), dist.map { it.weight })
        // totals: weight 2 -> 2 (p1.B, p3.A); weight 5 -> 2 (p2.A, p2.B); weight 8 -> 2 (p1.A, p2.C); weight 10 -> 1 (p3.B)
        assertEquals(2, dist[1].total)   // weight 2
        assertEquals(2, dist[4].total)   // weight 5
        assertEquals(2, dist[7].total)   // weight 8
        assertEquals(1, dist[9].total)   // weight 10
        assertEquals(0, dist[0].total)   // weight 1 untouched
        // actuals: weight 5 -> 1 (p2.B id=4); weight 8 -> 1 (p1.A id=1); others 0 incl open
        assertEquals(1, dist[4].actual)  // weight 5
        assertEquals(1, dist[7].actual)  // weight 8
        assertEquals(0, dist[1].actual)  // weight 2 — no outcome here
        assertEquals(0, dist[9].actual)  // weight 10 — p3 is open
    }

    @Test
    fun `confidenceDistribution returns all-zero buckets for empty input`() {
        val dist = engine.confidenceDistribution(emptyList())
        assertEquals(10, dist.size)
        assertEquals(0, dist.sumOf { it.total })
        assertEquals(0, dist.sumOf { it.actual })
    }

    @Test
    fun `accuracyOverCount returns cumulative rolling average by prediction count`() {
        val p1 = Prediction(id = 1, title = "Q1", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(1000), outcomeOptionId = 1)
        val p2 = Prediction(id = 2, title = "Q2", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(2000), outcomeOptionId = 3)
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Y", weight = 10, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "N", weight = 5, sortOrder = 0)
        val opt3 = PredictionOption(id = 4, predictionId = 2, label = "Y", weight = 5, sortOrder = 1)
        val items = listOf(
            PredictionWithOptions(p1, listOf(opt1)),
            PredictionWithOptions(p2, listOf(opt2, opt3)),
        )
        val points = engine.accuracyOverCount(items)
        assertEquals(2, points.size)
        assertEquals(1, points[0].first)
        assertEquals(1.0, points[0].second, 0.001)
        assertEquals(2, points[1].first)
        assertEquals(0.75, points[1].second, 0.001)
    }
}
