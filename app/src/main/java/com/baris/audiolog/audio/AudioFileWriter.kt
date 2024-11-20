package com.baris.audiolog.audio

import android.content.Context
import android.util.Log
import com.baris.audiolog.audio.enums.AudioFileFormat
import java.io.File
import java.io.FileOutputStream

class AudioFileWriter(private val context: Context, private val outputDirectory: File) : IAudioFileWriter {
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

    override fun write(buffer: ShortArray, fileName: String, audioFormat: Int) {
        try {
            if (outputStream == null) {
                // Ensure directory exists before creating the file
                ensureDirectoryExists()

                // Get the file extension from the enum
                val fileFormat = AudioFileFormat.fromEncoding(audioFormat)
                val fileExtension = fileFormat.extension

                // Create the output file with the correct extension
                outputFile = File(outputDirectory, "$fileName.$fileExtension")
                Log.d("FileAudioFileWriter", "Output file path: ${outputFile!!.absolutePath}")

                Log.d("FileAudioFileWriter", "Trying to create file: ${outputFile!!.absolutePath}")
                outputStream = FileOutputStream(outputFile)
                Log.d("FileAudioFileWriter", "OutputStream initialized successfully")

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
    override fun setOutputFile(fileName: String, audioFormat: Int) {
        // Extract the base name and ensure it has the correct extension
//        val baseName = fileName.substringBeforeLast(".")
//        val desiredExtension = fileName.substringAfterLast(".", "wav")
//        outputFile = File(outputDirectory, "$baseName.$desiredExtension")
        outputFile = File(outputDirectory, "${fileName}.${audioFormat}")
        Log.d("FileAudioFileWriter", "Set output file to: ${outputFile!!.absolutePath}")
    }

    override fun clearCache() {
        try {
            if (outputDirectory.exists() && outputDirectory.isDirectory) {
                val deletedFiles = outputDirectory.listFiles()?.map { file ->
                    val deleted = file.delete()
                    Log.d("FileAudioFileWriter", "Deleted ${file.name}: $deleted")
                    deleted
                } ?: listOf()

                if (deletedFiles.isNotEmpty()) {
                    Log.d("FileAudioFileWriter", "Cache cleared successfully")
                } else {
                    Log.d("FileAudioFileWriter", "No files to delete in cache")
                }
            } else {
                Log.d("FileAudioFileWriter", "Output directory does not exist or is not a directory")
            }
        } catch (e: Exception) {
            Log.e("FileAudioFileWriter", "Failed to clear cache: ${e.message}")
        }
    }
}