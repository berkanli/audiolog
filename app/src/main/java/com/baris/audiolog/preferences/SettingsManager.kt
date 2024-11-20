package com.baris.audiolog.preferences

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioFormat
import android.util.Log

class SettingsManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFERENCES_NAME = "audio_log_preferences"
        private const val KEY_AUDIO_FORMAT = "audio_format"
        private const val DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT // Default format
    }

    // Function to save the audio format preference
    fun saveAudioFormat(format: Int) {
        // Save the audio format as an integer
        sharedPreferences.edit().putInt(KEY_AUDIO_FORMAT, format).apply()
    }

    // Function to retrieve the saved audio format, defaulting to PCM if not set
    fun getAudioFormat(): Int {
        // Try to retrieve the format as an integer
        val storedFormat = sharedPreferences.getString(KEY_AUDIO_FORMAT, null)

        // Check if the stored value is a valid integer
        return if (storedFormat != null && storedFormat.toIntOrNull() != null) {
            storedFormat.toInt()  // If it's valid, return the integer value
        } else {
            // If the stored value is invalid, reset the preference and return the default value
            sharedPreferences.edit().remove(KEY_AUDIO_FORMAT).apply()  // Remove corrupted data
            Log.d("SettingsManager", "Invalid or missing audio format, returning default: $DEFAULT_AUDIO_FORMAT")
            DEFAULT_AUDIO_FORMAT  // Return default audio format
        }
    }

    // Function to clear all saved preferences
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}