package com.baris.audiolog.audio

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileAudioFileWriter(private val context: Context, private val outputDirectory: File) : AudioFileWriter {
    private var outputStream: FileOutputStream? = null
    private var outputFile: File? = null

    init {
        // Ensure the output directory exists
        ensureDirectoryExists()
    }

    // Method to ensure directory exists or is created
    private fun ensureDirectoryExists() {
        if (!outputDirectory.exists()) {
            val created = outputDirectory.mkdirs()
            Log.d("FileAudioFileWriter", "Directory created: $created")
            if (!created) {
                Log.e("FileAudioFileWriter", "Failed to create directory: ${outputDirectory.absolutePath}")
            }
        }else {
            Log.d("FileAudioFileWriter", "Directory already exists: ${outputDirectory.absolutePath}")
        }
    }

    override fun write(buffer: ShortArray) {
        try {
            if (outputStream == null) {
                // Ensure directory exists before creating the file
                ensureDirectoryExists()

                outputFile = File(outputDirectory, "recording.wav")
                Log.d("FileAudioFileWriter", "Output file path: ${outputFile!!.absolutePath}")

                // Create file output stream
                outputStream = FileOutputStream(outputFile)
                Log.d("FileAudioFileWriter", "Writing to file: ${outputFile!!.absolutePath}")

                // After opening the output stream
                if (outputFile!!.exists()) {
                    Log.d("FileAudioFileWriter", "File created successfully: ${outputFile!!.absolutePath}")
                } else {
                    Log.e("FileAudioFileWriter", "Failed to create file: ${outputFile!!.absolutePath}")
                }
            }

            // Convert short array to byte array and write
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

    // Set the file for recording dynamically
    override fun setOutputFile(fileName: String) {
        outputFile = File(outputDirectory, fileName)
    }
}