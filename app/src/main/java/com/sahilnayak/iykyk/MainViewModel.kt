package com.sahilnayak.iykyk

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sahilnayak.iykyk.model.ProcessingState
import com.sahilnayak.iykyk.processing.VideoProcessor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val videoProcessor = VideoProcessor(application)
    private val mutableState = MutableStateFlow<ProcessingState>(ProcessingState.Empty)
    val state: StateFlow<ProcessingState> = mutableState.asStateFlow()
    private var processingJob: Job? = null

    fun selectVideo(uri: Uri) {
        processingJob?.cancel()
        releaseCurrentResult()
        process(uri)
    }

    override fun onCleared() {
        releaseCurrentResult()
        videoProcessor.close()
    }

    private fun releaseCurrentResult() {
        // Result portraits are intentionally kept only while their collage is on screen.
        val current = mutableState.value
        if (current is ProcessingState.Complete) {
            current.result.people.forEach { it.portrait.recycle() }
            current.result.collage.recycle()
        }
    }

    private fun process(uri: Uri) {
        processingJob = viewModelScope.launch {
            try {
                mutableState.value = ProcessingState.Working(0f, "Reading video")
                val result = videoProcessor.process(uri) { progress, message ->
                    mutableState.value = ProcessingState.Working(progress, message)
                }
                mutableState.value = ProcessingState.Complete(result)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value = ProcessingState.Failed(error.message ?: "Could not process this video")
            }
        }
    }
}
