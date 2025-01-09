package com.baris.audiolog.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.baris.audiolog.R
import com.baris.audiolog.audio.AudioFileWriter
import com.baris.audiolog.audio.AudioFilesManager
import com.baris.audiolog.audio.Recorder
import com.baris.audiolog.preferences.SettingsManager
import com.baris.audiolog.ui.components.AudioFilesList
import com.baris.audiolog.ui.components.FileSaveDialog
import com.baris.audiolog.ui.components.RecordingTimer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    context: Context,
    recorder: Recorder,
    settingsManager: SettingsManager,
    audioFilesManager: AudioFilesManager,
    audioFileWriter: AudioFileWriter,
    onSettingsClicked: () -> Unit,
) {
    var isRecording by remember { mutableStateOf(false) }
    var showFileSaveScreen by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf( "") }

    // State to store audio data once recording is stopped
    var audioData by remember { mutableStateOf<ByteArray?>(null) }

    if (showFileSaveScreen) {
        // File Save Screen
        FileSaveDialog(
            fileName = fileName,
            onFileNameChange = { fileName = it },
            onSave = {
                recorder.saveAndClose()
                showFileSaveScreen = false
            },
            onDelete = {
                recorder.delete()
                showFileSaveScreen = false
            },
            onDismiss = {}
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
                        IconButton(onClick = onSettingsClicked ) {
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

                            // Stop recording and retrieve audio data
                            recorder.stop()
                            Log.d("RecorderScreen", "Stop button clicked.")
                            audioData = recorder.getAudioData() // Capture audio data after stopping
                            isRecording = false
                            fileName = audioFileWriter.getFileName() ?: ""
                            showFileSaveScreen = true // Navigate to file save screen

                        } else {
                            // Start recording
                            recorder.start()
                            isRecording = true
                        }
                    }
                ) {
                    if (isRecording) {
                        Icon(painter = painterResource(id = R.drawable.pause100), contentDescription = "Pause")
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer
                RecordingTimer(isRecording = isRecording)

                Spacer(modifier = Modifier.height(16.dp))

                // Waveform Visualizer
                //RealTimeWaveformVisualizer(recorder)

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Files List

                AudioFilesList(audioFilesManager)
            }
        }
    }
}

