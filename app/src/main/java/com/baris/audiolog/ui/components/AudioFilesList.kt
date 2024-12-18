package com.baris.audiolog.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baris.audiolog.audio.Recorder

@Composable
fun AudioFilesList(context: Context, recorder: Recorder) {
    // Get the saved audio files
    var audioFiles by remember { mutableStateOf(recorder.getSavedAudioFiles(context)) }

    // LazyColumn to show the list of files
    LazyColumn {
        items(audioFiles) { file ->
            Text(
                text = file.name,
                modifier = Modifier
                    .clickable {
                        // Handle the file click, e.g., show options to export or copy

                    }
                    .padding(16.dp)
            )
        }
    }
}