package com.baris.audiolog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.baris.audiolog.audio.IAudioFileWriter
import com.baris.audiolog.preferences.SettingsManager
import com.baris.audiolog.ui.components.AudioFormatDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    audioFileWriter: IAudioFileWriter,
    onThemeChange: (Boolean) -> Unit,
    onArrowIconClicked: () -> Unit
) {
    // Retrieve the saved audio format
    var selectedFormat by remember {
        mutableStateOf(settingsManager.getAudioFormat())
    }

    var isDarkThemeEnabled by remember {
        mutableStateOf(settingsManager.isDarkThemeEnabled())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = { onArrowIconClicked }) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Settings Screen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Enable Dark Theme")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDarkThemeEnabled,
                    onCheckedChange = { isEnabled ->
                        isDarkThemeEnabled = isEnabled
                        onThemeChange(isEnabled)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AudioFormatDropdown(
                selectedFormat = selectedFormat,
                onFormatChange = { newFormat ->
                    selectedFormat = newFormat
                    settingsManager.saveAudioFormat(newFormat) // Save the user's choice
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { audioFileWriter.clearCache() }) {
                Text("Clear Cache")
            }
        }
    }
}
