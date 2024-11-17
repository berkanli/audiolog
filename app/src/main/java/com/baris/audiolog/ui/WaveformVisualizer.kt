package com.baris.audiolog.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    buffer: ShortArray,
    modifier: Modifier = Modifier.fillMaxWidth().height(100.dp),
    color: Color = Color.Blue,
    strokeWidth: Float = 2f
) {
    Canvas(modifier = modifier) {
        val midY = size.height / 2
        val step = maxOf(1, buffer.size / size.width.toInt()) // Step for down-sampling

        for (i in buffer.indices step step) {
            val x = i.toFloat() * size.width / buffer.size // Convert index to x-coordinate
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
