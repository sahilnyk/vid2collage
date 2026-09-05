package com.sahilnayak.iykyk.processing

import android.graphics.Bitmap
import com.sahilnayak.iykyk.model.FaceObservation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class AppearanceTrackerTest {
    @Test
    fun nearbyMatchesFormOneAppearance() {
        val tracker = AppearanceTracker(maximumGapMs = 900, continuationSimilarity = 0.8f)
        tracker.add(listOf(observation(0, 3, floatArrayOf(1f, 0f))), 0)
        tracker.add(listOf(observation(250, 3, floatArrayOf(0.99f, 0.1f))), 250)

        val appearances = tracker.finish()

        assertEquals(1, appearances.size)
        assertEquals(0, appearances.single().startMs)
        assertEquals(250, appearances.single().endMs)
    }

    @Test
    fun longGapStartsAnotherAppearance() {
        val tracker = AppearanceTracker(maximumGapMs = 900, continuationSimilarity = 0.8f)
        tracker.add(listOf(observation(0, 3, floatArrayOf(1f, 0f))), 0)
        tracker.add(listOf(observation(1_100, 4, floatArrayOf(1f, 0f))), 1_100)

        assertEquals(2, tracker.finish().size)
    }

    @Test
    fun missingSampleEndsTheDefaultAppearance() {
        val tracker = AppearanceTracker()
        tracker.add(listOf(observation(0, 3, floatArrayOf(1f, 0f))), 0)
        tracker.add(emptyList(), 250)
        tracker.add(listOf(observation(500, 4, floatArrayOf(1f, 0f))), 500)

        assertEquals(2, tracker.finish().size)
    }

    @Test
    fun reusedTrackingIdCannotJoinDifferentFaces() {
        val tracker = AppearanceTracker(maximumGapMs = 900, continuationSimilarity = 0.8f)
        tracker.add(listOf(observation(0, 7, floatArrayOf(1f, 0f))), 0)
        tracker.add(listOf(observation(250, 7, floatArrayOf(0f, 1f))), 250)

        assertEquals(2, tracker.finish().size)
    }

    private fun observation(timestampMs: Long, trackingId: Int, embedding: FloatArray) = FaceObservation(
        timestampMs = timestampMs,
        trackingId = trackingId,
        embedding = embedding,
        portrait = mock(Bitmap::class.java),
        quality = 0.8f
    )
}
