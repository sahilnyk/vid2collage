package com.sahilnayak.iykyk.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
                drawHeader(this, people)
                drawPeople(this, people)
                drawFooter(this)
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(CREAM)
        canvas.drawRect(0f, 0f, 22f, HEIGHT.toFloat(), solidPaint(FOREST))
        canvas.drawRect(22f, 0f, 30f, HEIGHT.toFloat(), solidPaint(PEACH))

        val grain = solidPaint(FOREST, 13)
        for (y in 18 until HEIGHT step 34) {
            val offset = if ((y / 34) % 2 == 0) 0f else 15f
            for (x in 45 until WIDTH step 34) canvas.drawCircle(x + offset, y.toFloat(), 1.1f, grain)
        }
    }

    private fun drawHeader(canvas: Canvas, people: List<PersonResult>) {
        val appearances = people.sumOf(PersonResult::appearanceCount)
        canvas.drawText("vid2collage", 66f, 76f, textPaint(26f, FOREST, Typeface.BOLD))
        canvas.drawText("the people", 64f, 157f, displayPaint(63f))
        canvas.drawText("in this film.", 64f, 218f, displayPaint(63f))

        val stat = RectF(760f, 62f, 1018f, 226f)
        canvas.drawRoundRect(stat, 34f, 34f, solidPaint(LILAC))
        canvas.drawText(people.size.toString().padStart(2, '0'), 792f, 158f, displayPaint(78f))
        canvas.drawText(if (people.size == 1) "person" else "people", 910f, 137f, textPaint(25f, FOREST, Typeface.BOLD))
        canvas.drawText("$appearances appearances", 792f, 194f, textPaint(22f, FOREST_SOFT, Typeface.NORMAL))

        canvas.drawText("one clear frame for every familiar face", 65f, 273f, textPaint(24f, FOREST_SOFT, Typeface.NORMAL))
        canvas.drawRect(65f, 300f, 1016f, 304f, solidPaint(FOREST))
    }

    private fun drawPeople(canvas: Canvas, people: List<PersonResult>) {
        val columns = when {
            people.size == 1 -> 1
            people.size <= 4 -> 2
            else -> 3
        }
        val rows = ceil(people.size.toFloat() / columns).toInt()
        val gap = if (columns == 3) 18f else 24f
        val horizontalPadding = if (columns == 1) 118f else 58f
        val availableWidth = WIDTH - horizontalPadding * 2
        val availableHeight = 1380f
        val cardWidth = (availableWidth - gap * (columns - 1)) / columns
        // Portraits stay generous even when the grid needs another row.
        val cardHeight = minOf(
            (availableHeight - gap * max(0, rows - 1)) / rows,
            cardWidth * 1.50f
        )
        val gridHeight = cardHeight * rows + gap * max(0, rows - 1)
        val startY = 332f + (availableHeight - gridHeight) / 2f

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
        val cardPath = organicPath(bounds, 30f, 14f, 34f, 14f)
        canvas.drawPath(cardPath, solidPaint(FOREST))

        val imageBounds = RectF(bounds.left + 7f, bounds.top + 7f, bounds.right - 7f, bounds.bottom - 78f)
        canvas.save()
        canvas.clipPath(organicPath(imageBounds, 24f, 10f, 20f, 10f))
        canvas.drawBitmap(person.portrait, sourceRect(person.portrait, imageBounds), imageBounds, IMAGE_PAINT)
        canvas.restore()

        val labelBounds = RectF(bounds.left + 7f, bounds.bottom - 72f, bounds.right - 7f, bounds.bottom - 7f)
        canvas.drawRoundRect(labelBounds, 11f, 11f, solidPaint(tint))
        val smallCard = bounds.width() < 340f
        canvas.drawText(
            "person ${person.id.toString().padStart(2, '0')}",
            bounds.left + 20f,
            bounds.bottom - 28f,
            textPaint(if (smallCard) 18f else 22f, FOREST, Typeface.BOLD)
        )
        val count = "${person.appearanceCount}×"
        val countPaint = textPaint(if (smallCard) 23f else 28f, FOREST, Typeface.BOLD)
        canvas.drawText(count, bounds.right - countPaint.measureText(count) - 20f, bounds.bottom - 27f, countPaint)
    }

    private fun drawFooter(canvas: Canvas) {
        val first = RectF(64f, 1775f, 610f, 1862f)
        val second = RectF(630f, 1775f, 1016f, 1862f)
        canvas.drawRoundRect(first, 22f, 22f, solidPaint(PISTACHIO))
        canvas.drawRoundRect(second, 22f, 22f, solidPaint(MINT))
        canvas.drawText("frames stay on your phone", 88f, 1827f, textPaint(23f, FOREST, Typeface.BOLD))
        canvas.drawText("one film, every return", 661f, 1827f, textPaint(21f, FOREST, Typeface.BOLD))
    }

    private fun organicPath(bounds: RectF, topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) =
        Path().apply {
            addRoundRect(
                bounds,
                floatArrayOf(
                    topLeft, topLeft,
                    topRight, topRight,
                    bottomRight, bottomRight,
                    bottomLeft, bottomLeft
                ),
                Path.Direction.CW
            )
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

    private fun displayPaint(size: Float) = textPaint(size, FOREST, Typeface.BOLD).apply {
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
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
        private val FOREST_SOFT = Color.rgb(36, 77, 65)
        private val CREAM = Color.rgb(255, 249, 237)
        private val PISTACHIO = Color.rgb(239, 248, 201)
        private val MINT = Color.rgb(201, 239, 218)
        private val LILAC = Color.rgb(232, 212, 245)
        private val PEACH = Color.rgb(245, 199, 169)
        private val CARD_COLORS = intArrayOf(MINT, LILAC, PEACH)
        private val IMAGE_PAINT = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }
}
