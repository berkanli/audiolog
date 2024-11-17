package com.baris.audiolog.preferences

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    // Keys for settings
    companion object {
        private const val PREFERENCES_NAME = "audio_log_preferences"
        private const val KEY_AUDIO_FORMAT = "audio_format"
    }

    // Function to save the audio format preference
    fun saveAudioFormat(format: AudioFormat) {
        sharedPreferences.edit().putString(KEY_AUDIO_FORMAT, format.value).apply()
    }

    // Function to retrieve the saved audio format, defaulting to PCM if not set
    fun getAudioFormat(): AudioFormat {
        val formatValue = sharedPreferences.getString(KEY_AUDIO_FORMAT, AudioFormat.PCM.value)
        return AudioFormat.entries.firstOrNull { it.value == formatValue } ?: AudioFormat.PCM
    }

    // Function to clear all saved preferences
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}