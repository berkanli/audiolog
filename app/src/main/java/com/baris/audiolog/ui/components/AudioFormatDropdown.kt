package com.baris.audiolog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baris.audiolog.audio.enums.AudioFileFormat

@Composable
fun AudioFormatDropdown(
    selectedFormat: Int,
    onFormatChange: (Int) -> Unit
) {
    val supportedFormats = AudioFileFormat.entries.toTypedArray()
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { expanded = true }
    ) {
        Text(
            text = "Audio Format: ${AudioFileFormat.fromEncoding(selectedFormat)}",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            supportedFormats.forEach { audioFormat ->
                DropdownMenuItem(
                    onClick = {
                        onFormatChange(audioFormat.encoding)
                        expanded = false
                    },
                    text = { Text(text = audioFormat.displayName) }
                )
            }
        }
    }
}
