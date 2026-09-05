package com.sahilnayak.iykyk

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sahilnayak.iykyk.processing.CollageExporter
import com.sahilnayak.iykyk.ui.Vid2CollageTheme
import com.sahilnayak.iykyk.ui.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val exporter by lazy { CollageExporter(applicationContext) }
    private var pendingSave: Bitmap? = null

    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        viewModel.selectVideo(uri)
    }

    private val storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val bitmap = pendingSave
        pendingSave = null
        if (granted && bitmap != null) save(bitmap) else toast("gallery permission is needed to save")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            Vid2CollageTheme {
                MainScreen(
                    viewModel = viewModel,
                    onPickVideo = { videoPicker.launch("video/*") },
                    onSave = ::requestSave,
                    onShare = ::share
                )
            }
        }
    }

    private fun requestSave(bitmap: Bitmap) {
        val needsPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingSave = bitmap
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            save(bitmap)
        }
    }

    private fun save(bitmap: Bitmap) {
        lifecycleScope.launch {
            runCatching { withContext(Dispatchers.IO) { exporter.save(bitmap) } }
                .onSuccess { toast("saved to gallery") }
                .onFailure { toast(it.message?.lowercase() ?: "could not save collage") }
        }
    }

    private fun share(bitmap: Bitmap) {
        lifecycleScope.launch {
            runCatching { withContext(Dispatchers.IO) { exporter.share(bitmap) } }
                .onSuccess(::startActivity)
                .onFailure { toast(it.message?.lowercase() ?: "could not share collage") }
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
