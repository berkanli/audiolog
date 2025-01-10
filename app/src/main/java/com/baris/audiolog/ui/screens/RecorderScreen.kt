package com.baris.audiolog.ui.screens

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
import com.baris.audiolog.R
import com.baris.audiolog.ui.components.FileSaveDialog
import com.baris.audiolog.ui.components.RecordingTimer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    onSettingsClicked: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSaveRecording: (fileName: String) -> Unit,
    onDeleteRecording: () -> Unit,
    getFileName: () -> String,
    audioFilesList: @Composable () -> Unit,
) {
    var fileName by remember { mutableStateOf(getFileName()) }
    var showFileSaveScreen by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    if (showFileSaveScreen) {
        FileSaveDialog(
            fileName = fileName,
            onFileNameChange = { newName -> fileName = newName },
            onSave = {
                onSaveRecording(fileName)
                showFileSaveScreen = false
            },
            onDelete = {
                onDeleteRecording()
                showFileSaveScreen = false
            },
            onDismiss = { showFileSaveScreen = false }
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
                        IconButton(onClick = onSettingsClicked) {
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
                            showFileSaveScreen = true
                            onStopRecording()
                            fileName = getFileName()
                            isRecording = false
                        } else {
                            onStartRecording()
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

                RecordingTimer(isRecording = isRecording)

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(16.dp))

                audioFilesList()
            }
        }
    }
}