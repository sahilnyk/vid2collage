package com.sahilnayak.iykyk.processing

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min

class FaceQualityScorer {
    fun evaluate(frame: Bitmap, face: Face): Float? {
        val box = face.boundingBox
        if (box.width() < MINIMUM_FACE_SIZE || box.height() < MINIMUM_FACE_SIZE) return null
        if (abs(face.headEulerAngleY) > MAXIMUM_YAW || abs(face.headEulerAngleZ) > MAXIMUM_ROLL) return null
        val focus = sharpness(frame, face)
        if (focus < MINIMUM_SHARPNESS) return null
        val pose = (1f - abs(face.headEulerAngleY) / 45f).coerceIn(0f, 1f) * 0.65f +
            (1f - abs(face.headEulerAngleZ) / 35f).coerceIn(0f, 1f) * 0.35f
        val eyes = listOfNotNull(face.leftEyeOpenProbability, face.rightEyeOpenProbability)
            .averageOrDefault(0.5f)
        val smile = face.smilingProbability ?: 0.5f
        val visible = visibleMargin(frame, face)
        return pose * 0.32f + focus * 0.30f + eyes * 0.20f + smile * 0.10f + visible * 0.08f
    }

    private fun sharpness(frame: Bitmap, face: Face): Float {
        val box = face.boundingBox
        val left = box.left.coerceIn(0, frame.width - 1)
        val top = box.top.coerceIn(0, frame.height - 1)
        val right = box.right.coerceIn(left + 1, frame.width)
        val bottom = box.bottom.coerceIn(top + 1, frame.height)
        val crop = Bitmap.createBitmap(frame, left, top, right - left, bottom - top)
        val sample = Bitmap.createScaledBitmap(crop, SAMPLE_SIZE, SAMPLE_SIZE, true)
        if (crop !== sample) crop.recycle()
        val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
        sample.getPixels(pixels, 0, SAMPLE_SIZE, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE)
        sample.recycle()

        var total = 0.0
        var totalSquared = 0.0
        var count = 0
        for (y in 1 until SAMPLE_SIZE - 1) {
            for (x in 1 until SAMPLE_SIZE - 1) {
                val center = luminance(pixels[y * SAMPLE_SIZE + x]) * 4
                val laplacian = center -
                    luminance(pixels[y * SAMPLE_SIZE + x - 1]) -
                    luminance(pixels[y * SAMPLE_SIZE + x + 1]) -
                    luminance(pixels[(y - 1) * SAMPLE_SIZE + x]) -
                    luminance(pixels[(y + 1) * SAMPLE_SIZE + x])
                total += laplacian
                totalSquared += laplacian * laplacian
                count++
            }
        }
        val variance = totalSquared / count - (total / count) * (total / count)
        return (ln(1.0 + variance) / ln(1.0 + SHARPNESS_CEILING)).toFloat().coerceIn(0f, 1f)
    }

    private fun visibleMargin(frame: Bitmap, face: Face): Float {
        val box = face.boundingBox
        val margin = min(
            min(box.left, frame.width - box.right).toFloat() / box.width(),
            min(box.top, frame.height - box.bottom).toFloat() / box.height()
        )
        return (margin / 0.18f).coerceIn(0f, 1f)
    }

    private fun luminance(pixel: Int): Int {
        val red = pixel shr 16 and 0xff
        val green = pixel shr 8 and 0xff
        val blue = pixel and 0xff
        return (red * 77 + green * 150 + blue * 29) shr 8
    }

    private fun List<Float>.averageOrDefault(default: Float): Float =
        if (isEmpty()) default else average().toFloat()

    companion object {
        private const val MINIMUM_FACE_SIZE = 72
        private const val MAXIMUM_YAW = 52f
        private const val MAXIMUM_ROLL = 42f
        private const val MINIMUM_SHARPNESS = 0.31f
        private const val SAMPLE_SIZE = 64
        private const val SHARPNESS_CEILING = 900.0
    }
}
