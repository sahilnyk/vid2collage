package com.sahilnayak.iykyk.processing

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.sahilnayak.iykyk.model.FaceObservation
import com.sahilnayak.iykyk.model.PersonResult
import com.sahilnayak.iykyk.model.ProcessingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class VideoProcessor(private val context: Context) : AutoCloseable {
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.07f)
            .enableTracking()
            .build()
    )
    private val embedder = FaceEmbedder(context)
    private val imageFactory = FaceImageFactory()
    private val qualityScorer = FaceQualityScorer()
    private val clusterer = IdentityClusterer()
    private val collageRenderer = CollageRenderer()

    suspend fun process(uri: Uri, onProgress: (Float, String) -> Unit): ProcessingResult =
        withContext(Dispatchers.Default) {
            VideoFrameReader(context, uri).use { reader ->
                val tracker = AppearanceTracker()
                try {
                    var processedFrames = 0
                    reader.frames().forEach { frame ->
                        try {
                            coroutineContext.ensureActive()
                            val faces = detector.process(InputImage.fromBitmap(frame.bitmap, 0)).await()
                            val observations = faces.mapNotNull { face ->
                                val quality = qualityScorer.evaluate(frame.bitmap, face) ?: return@mapNotNull null
                                val embeddingCrop = imageFactory.embeddingCrop(frame.bitmap, face)
                                val embedding = embedder.embed(embeddingCrop)
                                embeddingCrop.recycle()
                                FaceObservation(
                                    timestampMs = frame.timestampMs,
                                    trackingId = face.trackingId,
                                    embedding = embedding,
                                    portrait = imageFactory.portraitCrop(frame.bitmap, face),
                                    quality = quality
                                )
                            }
                            tracker.add(observations, frame.timestampMs)
                            processedFrames++
                            val progress = processedFrames.toFloat() / reader.frameCount
                            onProgress(progress.coerceIn(0f, 1f), "Finding clear faces")
                        } finally {
                            frame.bitmap.recycle()
                        }
                    }

                    // A face must stay clear across several samples; whip-pan flashes do not count.
                    val appearances = tracker.finish().filter {
                        it.endMs - it.startMs >= MINIMUM_APPEARANCE_DURATION_MS
                    }
                    check(appearances.isNotEmpty()) { "No clear faces found in this video" }
                    onProgress(0.94f, "Grouping familiar faces")
                    val clusters = clusterer.cluster(appearances)
                    val people = clusters.mapIndexed { index, cluster ->
                        val best = cluster.maxBy { it.quality }
                        cluster.filter { it !== best }.forEach { it.portrait.recycle() }
                        PersonResult(
                            id = index + 1,
                            appearanceCount = countSegments(cluster.map { it.startMs to it.endMs }),
                            portrait = best.portrait
                        )
                    }
                    onProgress(0.98f, "Building your collage")
                    ProcessingResult(
                        people = people,
                        collage = collageRenderer.render(people)
                    )
                } catch (error: Exception) {
                    tracker.clear()
                    throw error
                }
            }
        }

    override fun close() {
        detector.close()
        embedder.close()
    }

    private fun countSegments(ranges: List<Pair<Long, Long>>): Int {
        val sorted = ranges.sortedBy { it.first }
        if (sorted.isEmpty()) return 0
        var count = 1
        var end = sorted.first().second
        sorted.drop(1).forEach { (start, currentEnd) ->
            if (start - end > AppearanceTracker.DEFAULT_MAXIMUM_GAP_MS) count++
            end = maxOf(end, currentEnd)
        }
        return count
    }

    companion object {
        private const val MINIMUM_APPEARANCE_DURATION_MS = 750L
    }
}
