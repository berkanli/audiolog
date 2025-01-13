package com.baris.audiolog.audio

import android.content.Context
import android.util.Log
import java.io.File

class AudioFilesManager(private val context: Context) {

    private val audioFilesDirectory: File = context.filesDir

    fun fetchSavedFiles(): List<File> {
        val filesList = mutableListOf<File>()
        audioFilesDirectory.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "pcm") {
                filesList.add(file)
            }
        }
        return filesList
    }

    fun deleteFile(fileName: String): Boolean {
        val fileToDelete = File(audioFilesDirectory, fileName)
        return if (fileToDelete.exists() && fileToDelete.isFile) {
            fileToDelete.delete()
        } else {
            false
        }
    }

    fun clearCache() {
        try {
            if (audioFilesDirectory.exists() && audioFilesDirectory.isDirectory) {
                val deletedFiles = audioFilesDirectory.listFiles()?.map { file ->
                    val deleted = file.delete()
                    Log.d("AudioFilesManager", "Deleted ${file.name}: $deleted")
                    deleted
                } ?: listOf()

                if (deletedFiles.isNotEmpty()) {
                    Log.d("AudioFilesManager", "Cache cleared successfully")
                } else {
                    Log.d("AudioFilesManager", "No files to delete in cache")
                }
            } else {
                Log.d(
                    "AudioFilesManager",
                    "Audio files directory does not exist or is not a directory"
                )
            }
        } catch (e: Exception) {
            Log.e("AudioFilesManager", "Failed to clear cache: ${e.message}")
        }
    }
}

