package com.baris.audiolog.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class Recorder(private val context: Context, private val audioFileWriter: IAudioFileWriter) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val temporaryBuffer = mutableListOf<ShortArray>()
    private val maxBufferChunks = 100 // Cap buffer size to prevent memory overuse

    fun start(fileName: String, audioFormat: Int) {
        if (isRecording) {
            Log.w("Recorder", "Recording is already in progress")
            return
        }

        // Clean up previous resources (if any)
        release()

        val sampleRate = 48000
        val channelConfig = AudioFormat.CHANNEL_IN_STEREO

        // Get the minimum buffer size for the specified configuration
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (bufferSize <= 0) {
            Log.e("Recorder", "Invalid buffer size: $bufferSize")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioFileWriter.setOutputFile(fileName, audioFormat)
            audioRecord?.startRecording()
            isRecording = true
            Log.i("Recorder", "Recording started")

            synchronized(temporaryBuffer) {
                temporaryBuffer.clear() // Reset the buffer
            }

            // Start a coroutine for reading audio data
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                Log.i("Recorder", "Recording coroutine started")
                val buffer = ShortArray(bufferSize)
                try {
                    while (isRecording) {
                        val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (readBytes > 0) {
                            synchronized(temporaryBuffer) {
                                if (temporaryBuffer.size >= maxBufferChunks) {
                                    temporaryBuffer.removeAt(0)
                                }
                                temporaryBuffer.add(buffer.copyOf())
                                Log.d("Recorder", "Temporary buffer size: ${temporaryBuffer.size}")
                            }

                            // Write the buffer to the file
                            audioFileWriter.write(buffer)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Recorder", "Error in recording coroutine: ${e.message}")
                } finally {
                    Log.i("Recorder", "Recording coroutine finished")
                }
            }

        } catch (e: SecurityException) {
            Log.e("Recorder", "Permission denied: ${e.message}")
        } catch (e: Exception) {
            Log.e("Recorder", "Failed to start recording: ${e.message}")
        }
    }

    fun stop() {
        //if (!isRecording) return

        try {
            isRecording = false
            recordingJob?.cancel() // Cancel the coroutine

            // Wait for the coroutine to complete any ongoing work
            runBlocking {
                recordingJob?.join()
            }

            synchronized(temporaryBuffer) {
                temporaryBuffer.clear() // Clear buffer to avoid stale data
            }

            audioRecord?.stop()
            Log.i("Recorder", "Recording stopped")
        } catch (e: Exception) {
            Log.e("Recorder", "Failed to stop recording: ${e.message}")
        }
    }

    fun release() {
        try {
            recordingJob?.cancel() // Cancel any ongoing coroutine
            audioRecord?.release()
            audioRecord = null
            isRecording = false
            recordingJob = null
            Log.i("Recorder", "Resources released")
        } catch (e: Exception) {
            Log.e("Recorder", "Failed to release resources: ${e.message}")
        }
    }

    fun saveFileInternally(context: Context, fileName: String, audioData: ByteArray) {
        val file = File(context.filesDir, fileName)
        file.outputStream().use { it.write(audioData) }
    }

    fun getSavedAudioFiles(context: Context): List<File> {
        val directory = context.filesDir  // Directory is located in app's internal storage
        Log.i("Recorder", "Files directory: ${context.filesDir.absolutePath}")
        directory.listFiles()?.forEach {
            Log.i("Recorder", "Found file: ${it.name}")
        }
        // Make sure the directory exists
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        // Return all files in the directory
        return directory.listFiles()?.toList() ?: emptyList()
    }

    fun getAudioData(): ByteArray? {
        return audioFileWriter.getRecordedData() // Call the method from the writer
    }

    fun deleteTemporaryBuffer() {
        synchronized(temporaryBuffer) {
            temporaryBuffer.clear()
        }
        Log.i("Recorder", "Temporary recording deleted")
    }

    fun getCombinedBuffer(): ShortArray {
        synchronized(temporaryBuffer) {
            return temporaryBuffer.flatMap { it.toList() }.toShortArray()
        }
    }
}