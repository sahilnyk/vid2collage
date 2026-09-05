package com.sahilnayak.iykyk.processing

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlin.math.roundToInt

class VideoFrameReader(
    context: Context,
    uri: Uri,
    private val sampleIntervalMs: Long = SAMPLE_INTERVAL_MS
) : AutoCloseable {
    private val retriever = MediaMetadataRetriever().apply { setDataSource(context, uri) }
    val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        ?.toLongOrNull()
        ?.coerceAtLeast(sampleIntervalMs)
        ?: error("Could not read video duration")
    val frameCount = (durationMs / sampleIntervalMs).toInt() + 1

    fun frames(): Sequence<VideoFrame> = sequence {
        var timestampMs = 0L
        while (timestampMs <= durationMs) {
            val source = retriever.getFrameAtTime(
                timestampMs * 1_000,
                MediaMetadataRetriever.OPTION_CLOSEST
            )
            if (source != null) yield(VideoFrame(timestampMs, scaleForDetection(source)))
            timestampMs += sampleIntervalMs
        }
    }

    override fun close() = retriever.release()

    private fun scaleForDetection(source: Bitmap): Bitmap {
        val largestSide = maxOf(source.width, source.height)
        if (largestSide <= MAXIMUM_FRAME_SIDE) return source
        val scale = MAXIMUM_FRAME_SIDE.toFloat() / largestSide
        val scaled = Bitmap.createScaledBitmap(
            source,
            (source.width * scale).roundToInt(),
            (source.height * scale).roundToInt(),
            true
        )
        source.recycle()
        return scaled
    }

    data class VideoFrame(val timestampMs: Long, val bitmap: Bitmap)

    companion object {
        const val SAMPLE_INTERVAL_MS = 250L
        private const val MAXIMUM_FRAME_SIDE = 1440
    }
}
