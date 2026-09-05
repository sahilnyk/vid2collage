package com.sahilnayak.iykyk.processing

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class VectorMathTest {
    @Test
    fun identicalVectorsHaveMaximumSimilarity() {
        val vector = floatArrayOf(0.2f, 0.4f, 0.8f)

        assertEquals(1f, VectorMath.cosineSimilarity(vector, vector), 0.0001f)
    }

    @Test
    fun perpendicularVectorsHaveNoSimilarity() {
        assertEquals(
            0f,
            VectorMath.cosineSimilarity(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f)),
            0.0001f
        )
    }

    @Test
    fun averageIsNormalized() {
        val average = VectorMath.normalizedAverage(
            listOf(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f))
        )

        assertArrayEquals(floatArrayOf(0.7071f, 0.7071f), average, 0.001f)
    }
}
