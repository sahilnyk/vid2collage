package com.sahilnayak.iykyk.model

import android.graphics.Bitmap

data class FaceObservation(
    val timestampMs: Long,
    val trackingId: Int?,
    val embedding: FloatArray,
    val portrait: Bitmap,
    val quality: Float
)

data class Appearance(
    val startMs: Long,
    val endMs: Long,
    val embedding: FloatArray,
    val portrait: Bitmap,
    val quality: Float
)

data class PersonResult(
    val id: Int,
    val appearanceCount: Int,
    val portrait: Bitmap
)

data class ProcessingResult(
    val people: List<PersonResult>,
    val collage: Bitmap
)

sealed interface ProcessingState {
    data object Empty : ProcessingState
    data class Working(val progress: Float, val message: String) : ProcessingState
    data class Complete(val result: ProcessingResult) : ProcessingState
    data class Failed(val message: String) : ProcessingState
}
