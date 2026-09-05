package com.sahilnayak.iykyk.processing

import kotlin.math.sqrt

object VectorMath {
    fun cosineSimilarity(left: FloatArray, right: FloatArray): Float {
        require(left.size == right.size)
        var dot = 0f
        var leftLength = 0f
        var rightLength = 0f
        for (index in left.indices) {
            dot += left[index] * right[index]
            leftLength += left[index] * left[index]
            rightLength += right[index] * right[index]
        }
        val denominator = sqrt(leftLength) * sqrt(rightLength)
        return if (denominator == 0f) 0f else dot / denominator
    }

    fun normalizedAverage(vectors: List<FloatArray>): FloatArray {
        require(vectors.isNotEmpty())
        val average = FloatArray(vectors.first().size)
        vectors.forEach { vector ->
            require(vector.size == average.size)
            vector.forEachIndexed { index, value -> average[index] += value }
        }
        var length = 0f
        average.forEach { length += it * it }
        val scale = sqrt(length)
        if (scale > 0f) average.indices.forEach { average[it] /= scale }
        return average
    }
}
