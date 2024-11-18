package com.baris.audiolog.audio

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileAudioFileWriter(private val context: Context, private val outputDirectory: File) : AudioFileWriter {
    private var outputStream: FileOutputStream? = null
    //private val outputFile: File = File(outputDirectory, "temp_audio_recording.pcm")
    private var outputFile: File? = null

    init {
        // Ensure the output directory exists
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
    }

    override fun write(buffer: ShortArray) {
        try {
            if (outputStream == null) {
                // Ensure the directory exists
                if (!outputDirectory.exists()) {
                    val created = outputDirectory.mkdirs()
                    Log.d("FileAudioFileWriter", "Directory created: $created")
                    if (!created) throw IOException("Failed to create directory: ${outputDirectory.absolutePath}")
                }

                // Create file
                outputFile = File(outputDirectory, "recording.wav")
                outputStream = FileOutputStream(outputFile)
                Log.d("FileAudioFileWriter", "Writing to file: ${outputFile!!.absolutePath}")
            }

            // Write data
            val byteBuffer = ByteArray(buffer.size * 2)
            for (i in buffer.indices) {
                byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
            }
            outputStream?.write(byteBuffer)
        } catch (e: Exception) {
            Log.e("FileAudioFileWriter", "Error writing audio: ${e.message}")
        }
    }

//    override fun write(buffer: ShortArray) {
//        try {
//            if (outputStream == null) {
//                // Open the output stream once the first write occurs
//                val file = File(outputDirectory, "recording.wav")
//                outputStream = FileOutputStream(file)
//                // Write WAV file header (if necessary)
//                // For simplicity, assuming uncompressed PCM data here.
//            }
//
//            // Write audio data to the file
//            val byteBuffer = ByteArray(buffer.size * 2) // PCM 16-bit audio
//            for (i in buffer.indices) {
//                byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
//                byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
//            }
//            outputStream?.write(byteBuffer)
//        } catch (e: Exception) {
//            Log.e("FileAudioFileWriter", "Error writing audio: ${e.message}")
//        }
//    }

//    override fun close() {
//        try {
//            outputStream?.flush()
//            outputStream?.close()
//        } catch (e: Exception) {
//            Log.e("FileAudioFileWriter", "Error closing file: ${e.message}")
//        }
//    }

    override fun close() {
        try {
            outputStream?.close()
            Log.d("FileAudioFileWriter", "File closed successfully")
        } catch (e: Exception) {
            Log.e("FileAudioFileWriter", "Error closing file: ${e.message}")
        } finally {
            outputStream = null
        }
    }

    override fun getRecordedData(): ByteArray? {
        return try {
            if (outputFile == null || !outputFile!!.exists()) {
                Log.e("FileAudioFileWriter", "File does not exist: ${outputFile?.absolutePath}")
                null
            } else {
                outputFile!!.readBytes()
            }
        } catch (e: Exception) {
            Log.e("FileAudioFileWriter", "Error reading file: ${e.message}")
            null
        }
    }

    // Set the file for recording
    override fun setOutputFile(fileName: String) {
        outputFile = File(outputDirectory, fileName)
    }
}