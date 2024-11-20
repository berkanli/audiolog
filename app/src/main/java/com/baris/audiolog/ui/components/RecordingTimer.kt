package com.baris.audiolog.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit


@Composable
fun RecordingTimer(isRecording: Boolean) {
    var startTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            startTime = System.nanoTime() - elapsedTime * 1_000_000L
            while (isRecording) {
                delay(16L) // Update every 16 milliseconds (~60 FPS)
                elapsedTime = (System.nanoTime() - startTime) / 1_000_000L
            }
        }
    }

    Text(
        text = formatTime(elapsedTime),
        fontSize = 48.sp
    )
}

fun formatTime(time: Long): String {
    val milliseconds = time % 1000
    val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60
    val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60
    val hours = TimeUnit.MILLISECONDS.toHours(time)
    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds)
}