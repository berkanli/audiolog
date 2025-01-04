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
import java.io.File

class Recorder(private val audioFileWriter: AudioFileWriter) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val temporaryBuffer = mutableListOf<ShortArray>()
    private val maxBufferChunks = 10

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

    fun start(fileName: String) {
        if (isRecording) {
            Log.w("Recorder", "Recording is already in progress")
            return
        }

        // Clean up previous resources (if any)
        release()

        val sampleRate = getSampleRate()
        val channelConfig = getChannelConfig()
        val audioFormat = getAudioFormat()

        // Get the minimum buffer size for the specified configuration
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e("Recorder", "Invalid buffer size: $bufferSize. Check sample rate, channel configuration, and audio format.")
            return
        } else {
            Log.d("Recorder", "Provided buffer size: $bufferSize")
        }

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

                // Fallback to a supported configuration
                //fallbackToSupportedConfiguration(fileName)
                return
            }

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

//    private fun fallbackToSupportedConfiguration(fileName: String) {
//        Log.i("Recorder", "Falling back to supported configuration")
//
//        // Try different configurations
//        val supportedSampleRates = arrayOf(44100, 22050, 16000, 11025, 8000)
//        val supportedChannelConfigs = arrayOf(AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO)
//        val supportedAudioFormats = arrayOf(AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT)
//
//        for (rate in supportedSampleRates) {
//            for (channel in supportedChannelConfigs) {
//                for (format in supportedAudioFormats) {
//                    val bufferSize = AudioRecord.getMinBufferSize(rate, channel, format)
//                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
//                        try {
//                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                                Log.e("Recorder", "Permission denied: RECORD_AUDIO")
//                                continue
//                            }
//
//                            audioRecord = AudioRecord(
//                                MediaRecorder.AudioSource.MIC,
//                                rate,
//                                channel,
//                                format,
//                                bufferSize
//                            )
//
//                            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
//                                audioFileWriter.setOutputFile(fileName, format)
//                                audioRecord?.startRecording()
//                                isRecording = true
//                                Log.i("Recorder", "Recording started with fallback configuration: sampleRate: $rate, channelConfig: $channel, audioFormat: $format, bufferSize: $bufferSize")
//
//                                synchronized(temporaryBuffer) {
//                                    temporaryBuffer.clear() // Reset the buffer
//                                }
//
//                                // Start a coroutine for reading audio data
//                                recordingJob = CoroutineScope(Dispatchers.IO).launch {
//                                    Log.i("Recorder", "Recording coroutine started")
//                                    val buffer = ShortArray(bufferSize)
//                                    try {
//                                        while (isRecording) {
//                                            val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
//                                            if (readBytes > 0) {
//                                                synchronized(temporaryBuffer) {
//                                                    if (temporaryBuffer.size >= maxBufferChunks) {
//                                                        temporaryBuffer.removeAt(0)
//                                                    }
//                                                    temporaryBuffer.add(buffer.copyOf())
//                                                    Log.d("Recorder", "Temporary buffer size: ${temporaryBuffer.size}")
//                                                }
//
//                                                // Write the buffer to the file
//                                                audioFileWriter.write(buffer)
//                                            }
//                                        }
//                                    } catch (e: Exception) {
//                                        Log.e("Recorder", "Error in recording coroutine: ${e.message}")
//                                    } finally {
//                                        Log.i("Recorder", "Recording coroutine finished")
//                                    }
//                                }
//
//                                return
//                            }
//                        } catch (e: SecurityException) {
//                            Log.e("Recorder", "Permission denied: ${e.message}")
//                        } catch (e: Exception) {
//                            Log.e("Recorder", "Failed to initialize AudioRecord with fallback configuration: ${e.message}")
//                        }
//                    }
//                }
//            }
//        }
//
//        Log.e("Recorder", "No supported configuration found")
//    }

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
        } catch (e: Exception) {
            Log.e("Recorder", "Failed to stop recording: ${e.message}")
        } finally {
            release()
        }
    }

    private fun release() {
        audioRecord?.release()
        audioRecord = null
        Log.i("Recorder", "AudioRecord released")
    }

    fun getAudioData(): ByteArray? {
        return audioFileWriter.getRecordedData()
    }
}