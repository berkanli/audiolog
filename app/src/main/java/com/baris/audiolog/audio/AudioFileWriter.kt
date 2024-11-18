package com.baris.audiolog.audio

interface AudioFileWriter {
    fun write(buffer: ShortArray)
    fun close()
    fun getRecordedData(): ByteArray?
    fun setOutputFile(fileName: String)
}