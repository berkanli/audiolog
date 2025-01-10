package com.baris.audiolog.audio

interface IAudioFileWriter {
    fun write(buffer: ShortArray)
    fun close()
    fun getRecordedData(): ByteArray?
    fun setOutputFile(fileName: String, audioFormat: Int)
}