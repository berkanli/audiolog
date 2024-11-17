package com.baris.audiolog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.baris.audiolog.audio.Recorder
import com.baris.audiolog.preferences.SettingsManager
import com.baris.audiolog.ui.RecorderScreen
import com.baris.audiolog.ui.SettingsScreen


@Composable
fun MyAppNavHost(recorder: Recorder, settingsManager: SettingsManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            RecorderScreen(recorder, navController, settingsManager)
        }
        composable("settings") {
            SettingsScreen(navController, settingsManager)
        }
    }
}


