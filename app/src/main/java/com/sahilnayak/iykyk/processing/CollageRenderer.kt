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
        // A 9:16 canvas drops straight into an Instagram Story or the assignment recording.
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
        canvas.drawColor(PAPER)
        canvas.drawRect(0f, 0f, 18f, HEIGHT.toFloat(), solidPaint(CORAL))
        canvas.drawCircle(1010f, 92f, 170f, solidPaint(MINT))
        canvas.drawCircle(970f, 1850f, 220f, solidPaint(CORAL, 26))

        val grain = solidPaint(INK, 14)
        for (y in 0 until HEIGHT step 32) {
            val offset = if ((y / 32) % 2 == 0) 0f else 12f
            for (x in 0 until WIDTH step 32) canvas.drawCircle(x + offset, y.toFloat(), 1.2f, grain)
        }
    }

    private fun drawHeader(canvas: Canvas, people: List<PersonResult>) {
        val totalAppearances = people.sumOf(PersonResult::appearanceCount)
        canvas.drawText("IYKYK  /  THE PEOPLE CUT", 64f, 88f, textPaint(24f, INK, Typeface.BOLD, 0.12f))
        canvas.drawText(
            people.size.toString().padStart(2, '0'),
            62f,
            244f,
            textPaint(128f, INK, Typeface.BOLD)
        )
        canvas.drawText(
            if (people.size == 1) "PERSON" else "PEOPLE",
            265f,
            195f,
            textPaint(54f, INK, Typeface.BOLD)
        )
        canvas.drawText(
            "$totalAppearances ${if (totalAppearances == 1) "appearance" else "appearances"} across one film",
            270f,
            241f,
            textPaint(26f, Color.argb(175, 16, 21, 22), Typeface.NORMAL)
        )
        canvas.drawRect(64f, 278f, 1016f, 282f, solidPaint(INK))
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
        val availableHeight = 1405f
        val cardWidth = (availableWidth - gap * (columns - 1)) / columns
        // Keeping every portrait generous matters more than squeezing the grid edge-to-edge.
        val cardHeight = minOf(
            (availableHeight - gap * max(0, rows - 1)) / rows,
            cardWidth * 1.50f
        )
        val gridHeight = cardHeight * rows + gap * max(0, rows - 1)
        val startY = 318f + (availableHeight - gridHeight) / 2f

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
        val accent = if (index % 2 == 0) CORAL else MINT
        val plate = RectF(bounds.left + 9f, bounds.top + 10f, bounds.right + 9f, bounds.bottom + 10f)
        canvas.drawRoundRect(plate, 22f, 22f, solidPaint(accent))
        canvas.drawRoundRect(bounds, 22f, 22f, solidPaint(INK))

        val imageBounds = RectF(bounds.left + 7f, bounds.top + 7f, bounds.right - 7f, bounds.bottom - 77f)
        canvas.save()
        canvas.clipPath(Path().apply { addRoundRect(imageBounds, 16f, 16f, Path.Direction.CW) })
        canvas.drawBitmap(person.portrait, sourceRect(person.portrait, imageBounds), imageBounds, IMAGE_PAINT)
        canvas.restore()

        val smallCard = bounds.width() < 340f
        canvas.drawText(
            "PERSON ${person.id.toString().padStart(2, '0')}",
            bounds.left + 20f,
            bounds.bottom - 25f,
            textPaint(if (smallCard) 17f else 21f, PAPER, Typeface.BOLD, 0.06f)
        )
        val count = "${person.appearanceCount}×"
        val countPaint = textPaint(if (smallCard) 22f else 27f, accent, Typeface.BOLD)
        canvas.drawText(count, bounds.right - countPaint.measureText(count) - 20f, bounds.bottom - 23f, countPaint)
    }

    private fun drawFooter(canvas: Canvas) {
        canvas.drawRect(64f, 1785f, 1016f, 1788f, solidPaint(INK, 80))
        canvas.drawText("FRAMES STAY ON YOUR PHONE", 64f, 1844f, textPaint(20f, INK, Typeface.BOLD, 0.11f))
        canvas.drawText("ONE FILM. EVERY RETURN.", 64f, 1882f, textPaint(18f, Color.argb(150, 16, 21, 22), Typeface.NORMAL, 0.08f))
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

    private fun textPaint(size: Float, color: Int, style: Int, spacing: Float = 0f) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = Typeface.create("sans-serif", style)
            letterSpacing = spacing
        }

    companion object {
        const val WIDTH = 1080
        const val HEIGHT = 1920
        private val INK = Color.rgb(16, 21, 22)
        private val PAPER = Color.rgb(245, 240, 231)
        private val CORAL = Color.rgb(255, 118, 87)
        private val MINT = Color.rgb(146, 216, 199)
        private val IMAGE_PAINT = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }
}
