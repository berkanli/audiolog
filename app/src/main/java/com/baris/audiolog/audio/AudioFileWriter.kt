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
            Log.d("FileAudioFileWriter", "Directory created: $created, Path: ${outputDirectory.absolutePath}")
        } else {
            Log.d("FileAudioFileWriter", "Directory already exists: ${outputDirectory.absolutePath}")
        }
    }

    override fun write(buffer: ShortArray) {
        try {
            // Ensure the output stream is initialized
            if (outputStream == null) {
                if (outputFile == null) {
                    throw IllegalStateException("Output file must be set using setOutputFile() before writing.")
                }

                Log.d("FileAudioFileWriter", "Initializing OutputStream for: ${outputFile!!.absolutePath}")
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
            outputFile?.let {
                if (!it.exists()) {
                    Log.e("FileAudioFileWriter", "File does not exist: ${it.absolutePath}")
                    null
                } else {
                    it.readBytes()
                }
            } ?: run {
                Log.e("FileAudioFileWriter", "Output file is not set.")
                null
            }
        } catch (e: Exception) {
            Log.e("FileAudioFileWriter", "Error reading file: ${e.message}")
            null
        }
    }

//    override fun getRecordedData(): ByteArray? {
//        return try {
//            if (outputFile == null || !outputFile!!.exists()) {
//                Log.e("FileAudioFileWriter", "File does not exist: ${outputFile?.absolutePath}")
//                null
//            } else {
//                outputFile!!.readBytes()
//            }
//        } catch (e: Exception) {
//            Log.e("FileAudioFileWriter", "Error reading file: ${e.message}")
//            null
//        }
//    }

    // Set the file for recording dynamically
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