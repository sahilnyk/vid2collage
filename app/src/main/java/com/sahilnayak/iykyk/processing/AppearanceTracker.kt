package com.sahilnayak.iykyk.processing

import com.sahilnayak.iykyk.model.Appearance
import com.sahilnayak.iykyk.model.FaceObservation

class AppearanceTracker(
    private val maximumGapMs: Long = DEFAULT_MAXIMUM_GAP_MS,
    private val continuationSimilarity: Float = DEFAULT_CONTINUATION_SIMILARITY
) {
    private val active = mutableListOf<Track>()
    private val finished = mutableListOf<Appearance>()

    fun add(observations: List<FaceObservation>, timestampMs: Long) {
        closeExpired(timestampMs)
        val unmatchedTracks = active.toMutableSet()
        observations.sortedByDescending(FaceObservation::quality).forEach { observation ->
            val track = bestTrack(observation, unmatchedTracks)
            if (track == null) {
                active += Track(observation)
            } else {
                track.add(observation)
                unmatchedTracks -= track
            }
        }
    }

    fun finish(): List<Appearance> {
        active.forEach { finished += it.toAppearance() }
        active.clear()
        return finished.sortedBy(Appearance::startMs)
    }

    fun clear() {
        active.forEach { it.recyclePortrait() }
        finished.forEach { it.portrait.recycle() }
        active.clear()
        finished.clear()
    }

    private fun closeExpired(timestampMs: Long) {
        val expired = active.filter { timestampMs - it.lastTimestampMs > maximumGapMs }
        expired.forEach { finished += it.toAppearance() }
        active -= expired.toSet()
    }

    private fun bestTrack(observation: FaceObservation, candidates: Set<Track>): Track? {
        val matchingTrackingId = observation.trackingId?.let { id ->
            candidates.firstOrNull { it.trackingId == id }
        }
        // ML Kit can reuse an ID after a cut, so identity still gets a quick sanity check.
        if (matchingTrackingId != null &&
            matchingTrackingId.similarity(observation) >= TRACKING_ID_SIMILARITY
        ) return matchingTrackingId
        return candidates
            .map { it to it.similarity(observation) }
            .filter { it.second >= continuationSimilarity }
            .maxByOrNull { it.second }
            ?.first
    }

    private class Track(first: FaceObservation) {
        val trackingId = first.trackingId
        val startTimestampMs = first.timestampMs
        var lastTimestampMs = first.timestampMs
            private set
        private val embeddings = mutableListOf(first.embedding)
        private var best = first

        fun similarity(observation: FaceObservation): Float =
            VectorMath.cosineSimilarity(VectorMath.normalizedAverage(embeddings), observation.embedding)

        fun add(observation: FaceObservation) {
            embeddings += observation.embedding
            lastTimestampMs = observation.timestampMs
            if (observation.quality > best.quality) {
                best.portrait.recycle()
                best = observation
            } else {
                observation.portrait.recycle()
            }
        }

        fun toAppearance() = Appearance(
            startMs = startTimestampMs,
            endMs = lastTimestampMs,
            embedding = VectorMath.normalizedAverage(embeddings),
            portrait = best.portrait,
            quality = best.quality
        )

        fun recyclePortrait() = best.portrait.recycle()
    }

    companion object {
        // One missed 250 ms sample is enough to mark a real break between appearances.
        const val DEFAULT_MAXIMUM_GAP_MS = 400L
        const val DEFAULT_CONTINUATION_SIMILARITY = 0.52f
        private const val TRACKING_ID_SIMILARITY = 0.52f
    }
}
