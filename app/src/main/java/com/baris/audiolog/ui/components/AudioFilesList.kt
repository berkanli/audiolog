package com.baris.audiolog.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioFilesList(
    audioFiles: List<File>,
    onDeleteFile: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Audio File") },
            text = { Text("Are you sure you want to delete this audio file?") },
            confirmButton = {
                TextButton(onClick = {
                    fileToDelete?.let { file ->
                        fileToDelete = null
                        showDeleteDialog = false
                        onDeleteFile(file.name)
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        items(audioFiles) { file ->
            Card(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            // Single tap: Open intent to look for a media player app
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    ),
                                    "audio/*"
                                )
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Open with"))
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "No app found to open this file.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onLongClick = {
                            // Long tap: Show delete confirmation dialog
                            fileToDelete = file
                            showDeleteDialog = true
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = file.name,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
