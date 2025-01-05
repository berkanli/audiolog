package com.baris.audiolog.audio

import android.content.Context
import java.io.File

class AudioFilesManager(private val context: Context) {

    // Directory where the audio files are saved
    private val audioFilesDirectory: File = context.filesDir

    // Function to fetch all saved audio files
    fun fetchSavedFiles(): List<File> {
        val filesList = mutableListOf<File>()
        audioFilesDirectory.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "pcm") {
                filesList.add(file)
            }
        }
        return filesList
    }
}

