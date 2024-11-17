package com.baris.audiolog.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Recorder(private val audioFileWriter: AudioFileWriter) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val temporaryBuffer = mutableListOf<ShortArray>()
    private val maxBufferChunks = 100 // Cap buffer size to prevent memory overuse

    fun start() {
        if (isRecording) {
            Log.w("Recorder", "Recording is already in progress")
            return
        }

        // Clean up previous resources (if any)
        release()

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

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
                            }
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
        if (!isRecording) return

        try {
            isRecording = false
            recordingJob?.cancel() // Cancel the coroutine

            // Wait for the coroutine to complete any ongoing work
            runBlocking {
                recordingJob?.join()
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

    fun saveRecording(fileName: String) {
        try {
            // Ensure all writes are completed
            runBlocking {
                recordingJob?.join() // Wait for the recording coroutine to finish
            }

            audioFileWriter.close()
            Log.i("Recorder", "Recording saved as $fileName")
        } catch (e: Exception) {
            Log.e("Recorder", "Failed to save recording: ${e.message}")
        }
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