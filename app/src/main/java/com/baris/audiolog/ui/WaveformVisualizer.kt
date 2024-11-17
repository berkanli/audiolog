package com.baris.audiolog.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.baris.audiolog.audio.Recorder
import kotlinx.coroutines.delay

@Composable
fun RealTimeWaveformVisualizer(recorder: Recorder) {
    val bufferState = remember { mutableStateOf(ShortArray(0)) }

    // Periodically fetch the buffer
    LaunchedEffect(recorder) {
        while (true) {
            val combinedBuffer = recorder.getCombinedBuffer()
            bufferState.value = combinedBuffer
            delay(16L) // Update at ~60 FPS
        }
    }

    WaveformVisualizer(buffer = bufferState.value)
}

@Composable
fun WaveformVisualizer(
    buffer: ShortArray,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .border(1.dp, Color.Gray), // For debugging layout
    color: Color = Color.Blue,
    strokeWidth: Float = 2f
) {
    Canvas(modifier = modifier) {
        val midY = size.height / 2
        val step = if (size.width > 0) maxOf(1, buffer.size / size.width.toInt()) else 1

        for (i in buffer.indices step step) {
            val x = i.toFloat() / buffer.size * size.width
            val amplitude = (buffer[i].toFloat() / Short.MAX_VALUE) * midY
            drawLine(
                color = color,
                start = Offset(x, midY - amplitude),
                end = Offset(x, midY + amplitude),
                strokeWidth = strokeWidth
            )
        }
    }
}
