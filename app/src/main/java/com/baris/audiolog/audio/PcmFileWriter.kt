package com.baris.audiolog.audio

import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class PcmFileWriter(private val outputStream: FileOutputStream): AudioFileWriter {
    override fun write(buffer: ShortArray) {
        try {
            val byteBuffer = ByteBuffer.allocate(buffer.size * 2) // 2 bytes per short
            buffer.forEach { byteBuffer.putShort(it) }
            outputStream.write(byteBuffer.array())
        } catch (e: IOException) {
            Log.e("PcmFileWriter", "Error writing to file: ${e.message}")
        }
    }

    override fun close() {
        try {
            outputStream.close()
        } catch (e: IOException) {
            Log.e("PcmFileWriter", "Error closing output stream: ${e.message}")
        }
    }
}