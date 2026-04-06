package com.wisdometer.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareImageRenderer {

    fun sharePredictionCard(
        context: Context,
        question: String,
        options: List<Pair<String, Int>>,  // label to probability
        isResolved: Boolean,
        actualOptionLabel: String?,
    ) {
        val bitmap = renderPredictionCard(question, options, isResolved, actualOptionLabel)
        shareImageBitmap(context, bitmap, "prediction")
    }

    fun shareProfileStats(
        context: Context,
        accuracy: Int,
        brierScore: Double,
        totalPredictions: Int,
        resolvedPredictions: Int,
    ) {
        val bitmap = renderProfileSummary(accuracy, brierScore, totalPredictions, resolvedPredictions)
        shareImageBitmap(context, bitmap, "profile")
    }

    private fun renderPredictionCard(
        question: String,
        options: List<Pair<String, Int>>,
        isResolved: Boolean,
        actualOptionLabel: String?,
    ): Bitmap {
        val width = 900
        val rowHeight = 80
        val height = 120 + options.size * rowHeight + 80
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply { color = 0xFFFAFAF8.toInt(); isAntiAlias = true }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val textPaint = Paint().apply {
            color = 0xFF1A1A1A.toInt(); textSize = 40f; isAntiAlias = true; isFakeBoldText = true
        }
        canvas.drawText(question.take(60), 40f, 70f, textPaint)

        val statusText = if (isResolved) "RESOLVED" else "OPEN"
        val statusBgColor = if (isResolved) 0xFFD4EDDA.toInt() else 0xFFFFF3CD.toInt()
        val statusTextColor = if (isResolved) 0xFF155724.toInt() else 0xFF856404.toInt()
        val statusPaint = Paint().apply { color = statusBgColor; isAntiAlias = true }
        canvas.drawRoundRect(RectF(700f, 30f, 860f, 80f), 8f, 8f, statusPaint)
        val statusTextPaint = Paint().apply { color = statusTextColor; textSize = 28f; isAntiAlias = true }
        canvas.drawText(statusText, 720f, 65f, statusTextPaint)

        val barColors = listOf(0xFF4A90D9.toInt(), 0xFF7EC8A4.toInt(), 0xFFE8A44A.toInt(), 0xFFD96A6A.toInt())
        options.forEachIndexed { i, (label, probability) ->
            val y = 120f + i * rowHeight
            val labelColor = if (label == actualOptionLabel) barColors[i % barColors.size] else 0xFF6B6B6B.toInt()
            val labelPaint = Paint().apply {
                color = labelColor; textSize = 32f; isAntiAlias = true
            }
            val prefix = if (label == actualOptionLabel) "✓ " else ""
            canvas.drawText("$prefix$label: $probability%", 40f, y + 30f, labelPaint)

            val barBg = Paint().apply { color = barColors[i % barColors.size]; alpha = 40; isAntiAlias = true }
            val barFg = Paint().apply { color = barColors[i % barColors.size]; isAntiAlias = true }
            val barTop = y + 40f
            val barBottom = y + 56f
            canvas.drawRoundRect(RectF(40f, barTop, 860f, barBottom), 6f, 6f, barBg)
            canvas.drawRoundRect(RectF(40f, barTop, 40f + 820f * probability / 100f, barBottom), 6f, 6f, barFg)
        }

        val footerPaint = Paint().apply { color = 0xFF6B6B6B.toInt(); textSize = 24f; isAntiAlias = true }
        canvas.drawText("Wisdometer", 40f, height - 20f, footerPaint)
        return bitmap
    }

    private fun renderProfileSummary(
        accuracy: Int,
        brierScore: Double,
        totalPredictions: Int,
        resolvedPredictions: Int,
    ): Bitmap {
        val width = 900
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply { color = 0xFFFAFAF8.toInt() }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val titlePaint = Paint().apply {
            color = 0xFF1A1A1A.toInt(); textSize = 48f; isAntiAlias = true; isFakeBoldText = true
        }
        canvas.drawText("My Prediction Accuracy", 40f, 80f, titlePaint)

        val bigPaint = Paint().apply {
            color = 0xFF4A90D9.toInt(); textSize = 100f; isAntiAlias = true; isFakeBoldText = true
        }
        canvas.drawText("$accuracy%", 40f, 220f, bigPaint)

        val subPaint = Paint().apply { color = 0xFF6B6B6B.toInt(); textSize = 36f; isAntiAlias = true }
        canvas.drawText("Brier Score: ${"%.2f".format(brierScore)}", 40f, 280f, subPaint)
        canvas.drawText("Total: $totalPredictions  •  Resolved: $resolvedPredictions", 40f, 330f, subPaint)

        val footerPaint = Paint().apply { color = 0xFF6B6B6B.toInt(); textSize = 28f; isAntiAlias = true }
        canvas.drawText("Wisdometer", 40f, 380f, footerPaint)
        return bitmap
    }

    private fun shareImageBitmap(context: Context, bitmap: Bitmap, filePrefix: String) {
        val imagesDir = File(context.cacheDir, "images").also { it.mkdirs() }
        val imageFile = File(imagesDir, "${filePrefix}_share.png")
        FileOutputStream(imageFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share prediction"))
    }
}
