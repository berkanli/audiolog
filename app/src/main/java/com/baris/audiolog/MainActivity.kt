package com.baris.audiolog

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baris.audiolog.audio.AudioFileWriter
import com.baris.audiolog.audio.Recorder
import com.baris.audiolog.navigation.AppNavHost
import com.baris.audiolog.preferences.SettingsManager
import com.baris.audiolog.ui.theme.AudioLogTheme
import android.Manifest
import com.baris.audiolog.audio.AudioFilesManager

class MainActivity : ComponentActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private lateinit var recorder: Recorder
    private lateinit var settingsManager: SettingsManager
    private lateinit var audioFilesManager: AudioFilesManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the output file for saving the audio data
        val audioFileWriter = AudioFileWriter(
            context = this, // Use the activity context
            outputDirectory = this.filesDir
        )

        recorder = Recorder(this, audioFileWriter)
        settingsManager = SettingsManager(this)
        audioFilesManager = AudioFilesManager(this)
        // Check for RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            // Permission is already granted, start recording
            startRecording()
        }

        setContent {
            val isDarkThemeEnabled =
                remember { mutableStateOf(settingsManager.isDarkThemeEnabled()) }

            AudioLogTheme(darkTheme = isDarkThemeEnabled.value) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        context = this,
                        recorder = recorder,
                        settingsManager = settingsManager,
                        audioFilesManager = audioFilesManager,
                        audioFileWriter = audioFileWriter,
                        onThemeChange = { isDark ->
                            isDarkThemeEnabled.value = isDark
                            settingsManager.saveDarkTheme(isDark) // Save the theme preference
                        }
                    )
                }
            }
        }
    }

    private fun startRecording() {
        // Start the recording process
        //TODO refactor this 
        recorder.start("example_filename")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, start recording
                startRecording()
                Log.d("MainActivity", "Start Recording...")
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // When the activity is stopped, stop and release resources
        recorder.stop()
        //recorder.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure the recorder resources are released when the activity is destroyed
        //recorder.release()
    }


}
