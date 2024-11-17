package com.baris.audiolog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.baris.audiolog.audio.Recorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(recorder: Recorder, navController: NavController) {
    var isRecording by remember { mutableStateOf(false) }
    var showFileSaveScreen by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }


    if (showFileSaveScreen) {
        // File Save Screen
        FileSaveScreen(
            fileName = fileName,
            onFileNameChange = { fileName = it },
            onSave = {
                recorder.saveRecording(fileName)
                showFileSaveScreen = false // Reset to the initial state
            },
            onDelete = {
                recorder.deleteTemporaryBuffer()
                showFileSaveScreen = false // Reset to the initial state
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Blue.copy(alpha = 0.3f)
                    ),
                    title = {
                        Text(text = "Recorder")
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        if (isRecording) {
                            recorder.stop()
                            isRecording = false
                            showFileSaveScreen = true // Navigate to file save screen
                        } else {
                            recorder.start()
                            isRecording = true
                        }
                    }
                ) {
                    Text(if (isRecording) "Stop Recording" else "Start Recording")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer
                RecordingTimer(isRecording = isRecording)

                Spacer(modifier = Modifier.height(16.dp))

                // Waveform Visualizer
                WaveformVisualizer(buffer = recorder.getCombinedBuffer())
            }
        }
    }
}