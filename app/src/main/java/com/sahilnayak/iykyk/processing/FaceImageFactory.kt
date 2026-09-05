package com.sahilnayak.iykyk.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class FaceImageFactory {
    fun embeddingCrop(frame: Bitmap, face: Face): Bitmap {
        alignedCrop(frame, face)?.let { return it }
        val box = expandedSquare(face.boundingBox, frame.width, frame.height, 1.35f)
        val crop = Bitmap.createBitmap(frame, box.left, box.top, box.width(), box.height())
        if (abs(face.headEulerAngleZ) < 2f) return crop
        val matrix = Matrix().apply { postRotate(-face.headEulerAngleZ) }
        val aligned = Bitmap.createBitmap(crop, 0, 0, crop.width, crop.height, matrix, true)
        crop.recycle()
        return centerSquare(aligned)
    }

    private fun alignedCrop(frame: Bitmap, face: Face): Bitmap? {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position ?: return null
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position ?: return null
        val nose = face.getLandmark(FaceLandmark.NOSE_BASE)?.position ?: return null
        val imageLeftEye = if (leftEye.x < rightEye.x) leftEye else rightEye
        val imageRightEye = if (leftEye.x < rightEye.x) rightEye else leftEye
        val source = floatArrayOf(
            imageLeftEye.x, imageLeftEye.y,
            imageRightEye.x, imageRightEye.y,
            nose.x, nose.y
        )
        val size = FaceEmbedder.INPUT_SIZE.toFloat()
        val target = floatArrayOf(
            size * 0.35f, size * 0.375f,
            size * 0.65f, size * 0.375f,
            size * 0.50f, size * 0.575f
        )
        val matrix = Matrix()
        if (!matrix.setPolyToPoly(source, 0, target, 0, 3)) return null
        return Bitmap.createBitmap(FaceEmbedder.INPUT_SIZE, FaceEmbedder.INPUT_SIZE, Bitmap.Config.ARGB_8888).also {
            Canvas(it).drawBitmap(frame, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
    }

    fun portraitCrop(frame: Bitmap, face: Face): Bitmap {
        val faceBox = face.boundingBox
        val width = min(frame.width.toFloat(), max(faceBox.width() * 3.1f, frame.width * 0.48f))
        val height = min(frame.height.toFloat(), width * 1.25f)
        val centerX = faceBox.exactCenterX()
        val centerY = faceBox.exactCenterY() + faceBox.height() * 0.38f
        val cropWidth = width.roundToInt().coerceAtMost(frame.width)
        val cropHeight = height.roundToInt().coerceAtMost(frame.height)
        val left = (centerX - cropWidth / 2f).roundToInt().coerceIn(0, frame.width - cropWidth)
        val top = (centerY - cropHeight / 2f).roundToInt().coerceIn(0, frame.height - cropHeight)
        val crop = Bitmap.createBitmap(
            frame,
            left,
            top,
            cropWidth,
            cropHeight
        )
        if (crop.width <= PORTRAIT_WIDTH) return crop
        val scaledHeight = (crop.height * PORTRAIT_WIDTH.toFloat() / crop.width).roundToInt()
        val scaled = Bitmap.createScaledBitmap(crop, PORTRAIT_WIDTH, scaledHeight, true)
        crop.recycle()
        return scaled
    }

    private fun expandedSquare(box: Rect, frameWidth: Int, frameHeight: Int, scale: Float): Rect {
        val size = (max(box.width(), box.height()) * scale).roundToInt()
            .coerceAtMost(min(frameWidth, frameHeight))
        val left = (box.exactCenterX() - size / 2f).roundToInt().coerceIn(0, frameWidth - size)
        val top = (box.exactCenterY() - size / 2f).roundToInt().coerceIn(0, frameHeight - size)
        return Rect(left, top, left + size, top + size)
    }

    private fun centerSquare(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val square = Bitmap.createBitmap(
            bitmap,
            (bitmap.width - size) / 2,
            (bitmap.height - size) / 2,
            size,
            size
        )
        if (square !== bitmap) bitmap.recycle()
        return square
    }

    companion object {
        private const val PORTRAIT_WIDTH = 720
    }
}
