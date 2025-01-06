package com.baris.audiolog.audio

import android.content.Context
import android.util.Log
import com.baris.audiolog.audio.enums.AudioFileFormat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            Log.d("AudioFileWriter", "Directory created: $created, Path: ${outputDirectory.absolutePath}")
        } else {
            Log.d("AudioFileWriter", "Directory already exists: ${outputDirectory.absolutePath}")
        }
    }

    override fun write(buffer: ShortArray) {
        try {
            // Ensure the output stream is initialized
            if (outputStream == null) {
                if (outputFile == null) {
                    throw IllegalStateException("Output file must be set using setOutputFile() before writing.")
                }

                Log.d("AudioFileWriter", "Initializing OutputStream for: ${outputFile!!.absolutePath}")
                outputStream = FileOutputStream(outputFile)
            }

            // Convert the short array to a byte array
            val byteBuffer = ByteArray(buffer.size * 2)
            for (i in buffer.indices) {
                byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
            }

            // Write the byte buffer to the file
            outputStream?.write(byteBuffer)
        } catch (e: Exception) {
            Log.e("AudioFileWriter", "Error writing audio: ${e.message}")
        }
    }

    override fun close() {
        try {
            outputStream?.close()
            Log.d("AudioFileWriter", "File closed successfully")
        } catch (e: Exception) {
            Log.e("AudioFileWriter", "Error closing file: ${e.message}")
        } finally {
            outputStream = null
        }
    }

    override fun getRecordedData(): ByteArray? {
        return try {
            outputFile?.let {
                if (!it.exists()) {
                    Log.e("AudioFileWriter", "File does not exist: ${it.absolutePath}")
                    null
                } else {
                    it.readBytes()
                }
            } ?: run {
                Log.e("AudioFileWriter", "Output file is not set.")
                null
            }
        } catch (e: Exception) {
            Log.e("AudioFileWriter", "Error reading file: ${e.message}")
            null
        }
    }

    fun getOutputFile(): File? {
        return outputFile
    }

//    // Set the file for recording dynamically
//    override fun setOutputFile(fileName: String, audioFormat: Int) {
//        try {
//            // Close any existing stream to avoid file conflicts
//            close()
//
//            // Ensure the directory exists
//            ensureDirectoryExists()
//
//            // Determine the correct extension from the audio format
//            val fileFormat = AudioFileFormat.fromEncoding(audioFormat)
//            val fileExtension = fileFormat.extension
//
//            // Set the output file name with the correct extension
//            outputFile = File(outputDirectory, "$fileName.$fileExtension")
//            Log.d("FileAudioFileWriter", "Set output file to: ${outputFile!!.absolutePath}")
//        } catch (e: Exception) {
//            Log.e("FileAudioFileWriter", "Failed to set output file: ${e.message}")
//        }
//    }

    override fun setOutputFile(audioFormat: Int) {
        // Create the output file
        val defaultFileName = LocalDateTime.now()!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val format = AudioFileFormat.fromEncoding(audioFormat)
        outputFile = File(outputDirectory, "${defaultFileName}.${format.extension}")
        Log.d("AudioFileWriter", "Output file set to: ${outputFile!!.absolutePath}")

        // Ensure the file is created
        if (!outputFile!!.exists()) {
            try {
                outputFile!!.createNewFile()
                Log.d("AudioFileWriter", "Output file created.")
            } catch (e: IOException) {
                Log.e("AudioFileWriter", "Error creating output file: ${e.message}")
            }
        }
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