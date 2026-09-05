package com.sahilnayak.iykyk.processing

import com.sahilnayak.iykyk.model.Appearance

class IdentityClusterer(
    private val similarityThreshold: Float = DEFAULT_SIMILARITY_THRESHOLD
) {
    fun cluster(appearances: List<Appearance>): List<List<Appearance>> {
        val clusters = appearances.map { mutableListOf(it) }.toMutableList()
        while (true) {
            val pair = closestPair(clusters) ?: break
            clusters[pair.second].forEach(clusters[pair.first]::add)
            clusters.removeAt(pair.second)
        }
        return clusters
            .map { it.sortedBy(Appearance::startMs) }
            .sortedBy { it.first().startMs }
    }

    private fun closestPair(clusters: List<List<Appearance>>): Pair<Int, Int>? {
        var bestPair: Pair<Int, Int>? = null
        var bestSimilarity = similarityThreshold
        for (left in clusters.indices) {
            for (right in left + 1 until clusters.size) {
                if (hasTemporalConflict(clusters[left], clusters[right])) continue
                val similarity = averageLinkSimilarity(clusters[left], clusters[right])
                if (similarity >= bestSimilarity) {
                    bestSimilarity = similarity
                    bestPair = left to right
                }
            }
        }
        return bestPair
    }

    private fun hasTemporalConflict(left: List<Appearance>, right: List<Appearance>): Boolean =
        // Two faces sharing a frame must remain two people, however similar the embeddings look.
        left.any { first ->
            right.any { second ->
                minOf(first.endMs, second.endMs) >= maxOf(first.startMs, second.startMs)
            }
        }

    private fun averageLinkSimilarity(left: List<Appearance>, right: List<Appearance>): Float {
        var similarity = 0f
        left.forEach { first ->
            right.forEach { second ->
                similarity += VectorMath.cosineSimilarity(first.embedding, second.embedding)
            }
        }
        return similarity / (left.size * right.size)
    }

    companion object {
        const val DEFAULT_SIMILARITY_THRESHOLD = 0.62f
    }
}
