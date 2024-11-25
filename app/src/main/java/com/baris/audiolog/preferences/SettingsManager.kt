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
        private const val KEY_DARK_THEME = "dark_theme"
    }

    fun saveAudioFormat(format: Int) {
        // Save the audio format as an integer
        sharedPreferences.edit().putInt(KEY_AUDIO_FORMAT, format).apply()
    }

    fun getAudioFormat(): Int {
        // Retrieve the audio format as an integer using getInt()
        val format = sharedPreferences.getInt(KEY_AUDIO_FORMAT, DEFAULT_AUDIO_FORMAT)
        Log.d("SettingsManager", "Retrieved audio format: $format")
        return format
    }

    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    fun saveDarkTheme(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }
}