package com.sahilnayak.iykyk.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.sahilnayak.iykyk.model.PersonResult
import kotlin.math.ceil
import kotlin.math.max

class CollageRenderer {
    fun render(people: List<PersonResult>): Bitmap {
        require(people.isNotEmpty())
        // A 9:16 canvas drops straight into a story or the assignment recording.
        return Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888).also { bitmap ->
            Canvas(bitmap).run {
                drawBackground(this)
                drawPeople(this, people)
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(CREAM)

        val grain = solidPaint(FOREST, 13)
        for (y in 18 until HEIGHT step 34) {
            val offset = if ((y / 34) % 2 == 0) 0f else 15f
            for (x in 18 until WIDTH step 34) canvas.drawCircle(x + offset, y.toFloat(), 1.1f, grain)
        }
    }

    private fun drawPeople(canvas: Canvas, people: List<PersonResult>) {
        val columns = if (people.size == 1) 1 else 2
        val rows = ceil(people.size.toFloat() / columns).toInt()
        val gap = 24f
        val horizontalPadding = if (columns == 1) 118f else 72f
        val availableWidth = WIDTH - horizontalPadding * 2
        val availableHeight = 1830f
        val cardWidth = (availableWidth - gap * (columns - 1)) / columns
        // Portraits stay generous even when the grid needs another row.
        val cardHeight = minOf(
            (availableHeight - gap * max(0, rows - 1)) / rows,
            cardWidth * 1.50f
        )
        val gridHeight = cardHeight * rows + gap * max(0, rows - 1)
        val startY = 45f + (availableHeight - gridHeight) / 2f

        people.forEachIndexed { index, person ->
            val row = index / columns
            val column = index % columns
            val rowItems = minOf(columns, people.size - row * columns)
            val rowWidth = cardWidth * rowItems + gap * max(0, rowItems - 1)
            val rowStartX = (WIDTH - rowWidth) / 2f
            val left = rowStartX + column * (cardWidth + gap)
            val top = startY + row * (cardHeight + gap)
            drawCard(canvas, person, RectF(left, top, left + cardWidth, top + cardHeight), index)
        }
    }

    private fun drawCard(canvas: Canvas, person: PersonResult, bounds: RectF, index: Int) {
        val tint = CARD_COLORS[index % CARD_COLORS.size]
        canvas.drawRoundRect(bounds, 28f, 28f, solidPaint(FOREST))

        val imageBounds = RectF(bounds.left + 14f, bounds.top + 14f, bounds.right - 14f, bounds.bottom - 94f)
        canvas.save()
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(imageBounds, 14f, 14f, android.graphics.Path.Direction.CW) })
        canvas.drawBitmap(person.portrait, sourceRect(person.portrait, imageBounds), imageBounds, IMAGE_PAINT)
        canvas.restore()

        val labelBounds = RectF(bounds.left + 14f, bounds.bottom - 86f, bounds.right - 14f, bounds.bottom - 14f)
        canvas.drawRoundRect(labelBounds, 14f, 14f, solidPaint(tint))
        canvas.drawText(
            "person ${person.id.toString().padStart(2, '0')}",
            bounds.left + 34f,
            bounds.bottom - 39f,
            textPaint(27f, FOREST, Typeface.BOLD)
        )
        val count = "${person.appearanceCount}×"
        val countPaint = textPaint(31f, FOREST, Typeface.BOLD)
        canvas.drawText(count, bounds.right - countPaint.measureText(count) - 34f, bounds.bottom - 38f, countPaint)
    }

    private fun sourceRect(bitmap: Bitmap, target: RectF): Rect {
        val sourceRatio = bitmap.width.toFloat() / bitmap.height
        val targetRatio = target.width() / target.height()
        return if (sourceRatio > targetRatio) {
            val width = (bitmap.height * targetRatio).toInt()
            val left = (bitmap.width - width) / 2
            Rect(left, 0, left + width, bitmap.height)
        } else {
            val height = (bitmap.width / targetRatio).toInt()
            val top = (bitmap.height - height) / 2
            Rect(0, top, bitmap.width, top + height)
        }
    }

    private fun solidPaint(color: Int, alpha: Int = 255) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.alpha = alpha
    }

    private fun textPaint(size: Float, color: Int, style: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create("sans-serif", style)
    }

    companion object {
        const val WIDTH = 1080
        const val HEIGHT = 1920
        private val FOREST = Color.rgb(18, 56, 46)
        private val CREAM = Color.rgb(255, 249, 237)
        private val MINT = Color.rgb(201, 239, 218)
        private val LILAC = Color.rgb(232, 212, 245)
        private val PEACH = Color.rgb(245, 199, 169)
        private val CARD_COLORS = intArrayOf(MINT, LILAC, PEACH)
        private val IMAGE_PAINT = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }
}
