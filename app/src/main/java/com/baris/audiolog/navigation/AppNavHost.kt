package com.baris.audiolog.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.baris.audiolog.audio.AudioFileWriter
import com.baris.audiolog.audio.AudioFilesManager
import com.baris.audiolog.audio.Recorder
import com.baris.audiolog.preferences.SettingsManager
import com.baris.audiolog.ui.components.AudioFilesList
import com.baris.audiolog.ui.screens.RecorderScreen
import com.baris.audiolog.ui.screens.SettingsScreen


@Composable
fun AppNavHost(
    context: Context,
    recorder: Recorder,
    settingsManager: SettingsManager,
    audioFilesManager: AudioFilesManager,
    audioFileWriter: AudioFileWriter,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            RecorderScreen(
                onSettingsClicked = { navController.navigate("settings") },
                onStartRecording = recorder::start,
                onStopRecording = recorder::stop,
                onSaveRecording = { fileName -> recorder.saveAndClose(fileName) },
                onDeleteRecording = recorder::delete,
                getFileName = { recorder.getStartDate() ?: "" },
                audioFilesList = {
                    AudioFilesList(audioFilesManager)
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onThemeChange = onThemeChange,
                onArrowIconClicked = { navController.popBackStack() },
                onFormatChange = {format -> settingsManager.saveAudioFormat(format)},
                onClearCacheClicked = audioFilesManager::clearCache,
                initialSelectedFormat = settingsManager.getAudioFormat(),
                initialIsDarkThemeEnabled = settingsManager.isDarkThemeEnabled()
            )
        }
    }
}


