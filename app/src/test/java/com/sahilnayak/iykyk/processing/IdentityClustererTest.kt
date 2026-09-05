package com.sahilnayak.iykyk.processing

import android.graphics.Bitmap
import com.sahilnayak.iykyk.model.Appearance
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class IdentityClustererTest {
    @Test
    fun mergesSimilarAppearancesAcrossTimeline() {
        val appearances = listOf(
            appearance(0, floatArrayOf(1f, 0f)),
            appearance(5_000, floatArrayOf(0.98f, 0.1f)),
            appearance(2_000, floatArrayOf(0f, 1f))
        )

        val clusters = IdentityClusterer(0.8f).cluster(appearances)

        assertEquals(2, clusters.size)
        assertEquals(listOf(0L, 5_000L), clusters.first().map(Appearance::startMs))
    }

    @Test
    fun averageLinkPreventsSingleOutlierFromJoiningCluster() {
        val appearances = listOf(
            appearance(0, floatArrayOf(1f, 0f)),
            appearance(1_000, floatArrayOf(0.98f, 0.2f)),
            appearance(2_000, floatArrayOf(0.7f, 0.7f))
        )

        val clusters = IdentityClusterer(0.85f).cluster(appearances)

        assertEquals(2, clusters.size)
    }

    @Test
    fun overlappingFacesNeverBecomeOnePerson() {
        val appearances = listOf(
            appearance(0, floatArrayOf(1f, 0f)),
            appearance(0, floatArrayOf(0.99f, 0.01f))
        )

        val clusters = IdentityClusterer(0.8f).cluster(appearances)

        assertEquals(2, clusters.size)
    }

    private fun appearance(timestampMs: Long, embedding: FloatArray) = Appearance(
        startMs = timestampMs,
        endMs = timestampMs + 500,
        embedding = embedding,
        portrait = mock(Bitmap::class.java),
        quality = 0.8f
    )
}
