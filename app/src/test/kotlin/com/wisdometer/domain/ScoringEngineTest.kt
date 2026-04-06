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
        options: List<Pair<String, Int>>,  // label to probability
        actualIndex: Int,
    ): PredictionWithOptions {
        val prediction = Prediction(
            id = 1L,
            question = "Q",
            createdAt = Instant.EPOCH,
            resolvedAt = Instant.EPOCH,
            outcomeOptionId = (actualIndex + 1).toLong(),
        )
        val predictionOptions = options.mapIndexed { i, (label, prob) ->
            PredictionOption(
                id = (i + 1).toLong(),
                predictionId = 1L,
                label = label,
                probability = prob,
                sortOrder = i,
            )
        }
        return PredictionWithOptions(prediction, predictionOptions)
    }

    @Test
    fun `simpleCloseness is 1 when 100 percent on correct outcome`() {
        val p = makePrediction(listOf("Yes" to 100), actualIndex = 0)
        assertEquals(1.0, engine.simpleCloseness(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness is 0_1 when 10 percent on correct outcome`() {
        val p = makePrediction(listOf("Yes" to 10, "No" to 90), actualIndex = 0)
        assertEquals(0.1, engine.simpleCloseness(listOf(p)), 0.001)
    }

    @Test
    fun `simpleCloseness averages across multiple predictions`() {
        val p1 = makePrediction(listOf("Yes" to 100), actualIndex = 0)  // 1.0
        val p2 = makePrediction(listOf("Yes" to 0, "No" to 100), actualIndex = 0)  // 0.0
        assertEquals(0.5, engine.simpleCloseness(listOf(p1, p2)), 0.001)
    }

    @Test
    fun `brierScore is 0 for perfect prediction`() {
        val p = makePrediction(listOf("Yes" to 100), actualIndex = 0)
        assertEquals(0.0, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `brierScore is 2 for worst prediction single option`() {
        // 0% on correct, 100% on wrong: (0-1)^2 + (1-0)^2 = 1 + 1 = 2
        val p = makePrediction(listOf("Yes" to 0, "No" to 100), actualIndex = 0)
        assertEquals(2.0, engine.brierScore(listOf(p)), 0.001)
    }

    @Test
    fun `brierScore is 0_5 for 50 percent on correct with two options`() {
        // (0.5-1)^2 + (0.5-0)^2 = 0.25 + 0.25 = 0.5
        val p = makePrediction(listOf("Yes" to 50, "No" to 50), actualIndex = 0)
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
        val p1 = Prediction(id = 1, question = "Q1", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 1L, tags = "career")
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 3L, tags = "finance")
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Yes", probability = 100, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "No", probability = 10, sortOrder = 0)
        val items = listOf(
            PredictionWithOptions(p1, listOf(opt1)),
            PredictionWithOptions(p2, listOf(opt2)),
        )
        assertEquals(1.0, engine.simpleClosenessForTag(items, "career"), 0.001)
        assertEquals(0.1, engine.simpleClosenessForTag(items, "finance"), 0.001)
    }

    @Test
    fun `brierScoreForTag filters by tag`() {
        val p1 = Prediction(id = 1, question = "Q1", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 1L, tags = "career")
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH, resolvedAt = Instant.EPOCH,
            outcomeOptionId = 3L, tags = "finance")
        // p1: 100% on correct → (1-1)^2 + 0 = 0.0
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Yes", probability = 100, sortOrder = 0)
        // p2: 50% on correct, 50% other → (0.5-1)^2 + 0.5^2 = 0.25 + 0.25 = 0.5
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "No", probability = 50, sortOrder = 0)
        val opt3 = PredictionOption(id = 4, predictionId = 2, label = "Yes", probability = 50, sortOrder = 1)
        val items = listOf(
            PredictionWithOptions(p1, listOf(opt1)),
            PredictionWithOptions(p2, listOf(opt2, opt3)),
        )
        assertEquals(0.0, engine.brierScoreForTag(items, "career"), 0.001)
        assertEquals(0.5, engine.brierScoreForTag(items, "finance"), 0.001)
    }

    @Test
    fun `avgConfidence is mean probability of top-ranked option`() {
        val p1 = Prediction(id = 1, question = "Q", createdAt = Instant.EPOCH)
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH)
        val opts1 = listOf(
            PredictionOption(id = 1, predictionId = 1, label = "Yes", probability = 80, sortOrder = 0),
            PredictionOption(id = 2, predictionId = 1, label = "No", probability = 20, sortOrder = 1),
        )
        val opts2 = listOf(
            PredictionOption(id = 3, predictionId = 2, label = "A", probability = 60, sortOrder = 0),
            PredictionOption(id = 4, predictionId = 2, label = "B", probability = 40, sortOrder = 1),
        )
        val items = listOf(PredictionWithOptions(p1, opts1), PredictionWithOptions(p2, opts2))
        // top ranked: 80 and 60, avg = 70
        assertEquals(70.0, engine.avgConfidence(items), 0.001)
    }

    @Test
    fun `accuracyOverTime returns cumulative rolling average by resolution date`() {
        val p1 = Prediction(id = 1, question = "Q1", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(1000), outcomeOptionId = 1)
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(2000), outcomeOptionId = 3)
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Y", probability = 100, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "N", probability = 50, sortOrder = 0)
        val items = listOf(PredictionWithOptions(p1, listOf(opt1)), PredictionWithOptions(p2, listOf(opt2)))
        val points = engine.accuracyOverTime(items)
        // point 0: resolvedAt=1000, cumulative = 1.0
        // point 1: resolvedAt=2000, cumulative = (1.0 + 0.5) / 2 = 0.75
        assertEquals(2, points.size)
        assertEquals(1.0, points[0].second, 0.001)
        assertEquals(0.75, points[1].second, 0.001)
    }

    @Test
    fun `accuracyOverCount returns cumulative rolling average by prediction count`() {
        val p1 = Prediction(id = 1, question = "Q1", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(1000), outcomeOptionId = 1)
        val p2 = Prediction(id = 2, question = "Q2", createdAt = Instant.EPOCH,
            resolvedAt = Instant.ofEpochMilli(2000), outcomeOptionId = 3)
        val opt1 = PredictionOption(id = 1, predictionId = 1, label = "Y", probability = 100, sortOrder = 0)
        val opt2 = PredictionOption(id = 3, predictionId = 2, label = "N", probability = 50, sortOrder = 0)
        val items = listOf(PredictionWithOptions(p1, listOf(opt1)), PredictionWithOptions(p2, listOf(opt2)))
        val points = engine.accuracyOverCount(items)
        assertEquals(2, points.size)
        assertEquals(1, points[0].first)
        assertEquals(1.0, points[0].second, 0.001)
        assertEquals(2, points[1].first)
        assertEquals(0.75, points[1].second, 0.001)
    }
}
