package com.baris.audiolog.audio

import android.util.Log
import com.baris.audiolog.audio.enums.AudioFileFormat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioFileWriter(private val outputDirectory: File) :
    IAudioFileWriter {
    private var outputStream: FileOutputStream? = null
    private var outputFile: File? = null
    private val temporaryBuffer = mutableListOf<ShortArray>()

    init {
        // Ensure the output directory exists
        ensureDirectoryExists()
    }

    // Method to ensure directory exists or is created
    private fun ensureDirectoryExists() {
        if (!outputDirectory.exists()) {
            val created = outputDirectory.mkdirs()
            Log.d(
                "AudioFileWriter",
                "Directory created: $created, Path: ${outputDirectory.absolutePath}"
            )
        } else {
            Log.d("AudioFileWriter", "Directory already exists: ${outputDirectory.absolutePath}")
        }
    }

    override fun write(buffer: ShortArray) {
        synchronized(temporaryBuffer) {
            temporaryBuffer.add(buffer.copyOf())
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

    override fun setOutputFile(fileName: String, audioFormat: Int) {
        // Create the output file
//        val defaultFileName =
//            LocalDateTime.now()!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val format = AudioFileFormat.fromEncoding(audioFormat)
        outputFile = File(outputDirectory, "${fileName}.${format.extension}")
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

        // Write the temporary buffer to the file
        synchronized(temporaryBuffer) {
            outputStream = FileOutputStream(outputFile)
            for (buffer in temporaryBuffer) {
                val byteBuffer = ByteArray(buffer.size * 2)
                for (i in buffer.indices) {
                    byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                    byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
                }
                outputStream?.write(byteBuffer)
            }
            temporaryBuffer.clear()
        }
    }

    fun clearBuffer() {
        synchronized(temporaryBuffer) {
            temporaryBuffer.clear()
            Log.d("AudioFileWriter", "Buffer cleared")
        }
    }
}