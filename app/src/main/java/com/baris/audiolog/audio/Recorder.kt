package com.baris.audiolog.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Recorder(private val context: Context, private val audioFileWriter: AudioFileWriter) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val temporaryBuffer = mutableListOf<ShortArray>()
    private val maxBufferChunks = 10
    var permissionsGranted = false

    companion object {
        private var _sampleRate = 48000
        private var _channelConfig = AudioFormat.CHANNEL_IN_STEREO
        private var _audioFormat = AudioFormat.ENCODING_PCM_16BIT

        fun setSampleRate(sampleRate: Int) {
            _sampleRate = sampleRate
        }

        fun setChannel(channel: Int) {
            _channelConfig = channel
        }

        fun setAudioFormat(format: Int) {
            _audioFormat = format
        }

        private fun getSampleRate() = _sampleRate
        private fun getChannelConfig() = _channelConfig
        private fun getAudioFormat() = _audioFormat
    }
    //TODO add save-close function
    fun start() {
        if (isRecording) {
            Log.w("Recorder", "Recording is already in progress")
            return
        }

        // Check permissions before proceeding
        if (!permissionsGranted) {
            Log.e("Recorder", "Permissions not granted")
            return
        }

        // Clean up previous resources (if any)
        release()

        val sampleRate = getSampleRate()
        val channelConfig = getChannelConfig()
        val audioFormat = getAudioFormat()
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("Recorder", "Failed to initialize AudioRecord with sampleRate: $sampleRate, channelConfig: $channelConfig, audioFormat: $audioFormat, bufferSize: $bufferSize")
                return
            }

            audioFileWriter.setOutputFile(audioFormat)
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
        if (!isRecording) return

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

            // Check if the file exists
            val outputFile = audioFileWriter.getOutputFile()
            if (outputFile != null && outputFile.exists()) {
                Log.i("Recorder", "File saved successfully at: ${outputFile.absolutePath}")
            } else {
                Log.e("Recorder", "File does not exist: ${outputFile?.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("Recorder", "Failed to stop recording: ${e.message}")
        }
    }

    private fun release() {
        audioRecord?.release()
        audioRecord = null
        Log.i("Recorder", "AudioRecord released")
    }

    fun saveAndClose() {
        stop()
        audioFileWriter.close()
        release()
        Log.i("Recorder", "Recording saved and closed")
    }

    fun delete() {
        audioFileWriter.deleteOutputFile()
        // Perform any additional cleanup if necessary
        release()
        Log.i("Recorder", "Recording data deleted")
    }

    fun getAudioData(): ByteArray? {
        return audioFileWriter.getRecordedData()
    }
}