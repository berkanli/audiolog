package com.baris.audiolog.audio

interface IAudioFileWriter {
    fun write(buffer: ShortArray, fileName: String, audioFormat: Int)
    fun close()
    fun getRecordedData(): ByteArray?
    fun setOutputFile(fileName: String, audioFormat: Int)
    fun clearCache()
}