package com.sahilnayak.iykyk.processing

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class FaceEmbedder(context: Context) : AutoCloseable {
    private val interpreter = Interpreter(
        context.assets.openFd(MODEL_FILE).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength
                )
            }
        },
        Interpreter.Options().apply { setNumThreads(4) }
    )
    private val input = ByteBuffer
        .allocateDirect(INPUT_SIZE * INPUT_SIZE * CHANNELS * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
    private val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)

    @Synchronized
    fun embed(face: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(face, INPUT_SIZE, INPUT_SIZE, true)
        scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        input.rewind()
        pixels.forEach { pixel ->
            input.putFloat(((pixel shr 16 and 0xff) - 127.5f) / 127.5f)
            input.putFloat(((pixel shr 8 and 0xff) - 127.5f) / 127.5f)
            input.putFloat(((pixel and 0xff) - 127.5f) / 127.5f)
        }
        if (scaled !== face) scaled.recycle()

        val output = Array(1) { FloatArray(EMBEDDING_SIZE) }
        interpreter.run(input, output)
        return normalize(output[0])
    }

    override fun close() = interpreter.close()

    private fun normalize(values: FloatArray): FloatArray {
        var length = 0f
        values.forEach { length += it * it }
        val scale = sqrt(length)
        if (scale > 0f) values.indices.forEach { values[it] /= scale }
        return values
    }

    companion object {
        const val MODEL_FILE = "facenet.tflite"
        const val INPUT_SIZE = 160
        const val EMBEDDING_SIZE = 128
        private const val CHANNELS = 3
    }
}
