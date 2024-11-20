package com.baris.audiolog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.baris.audiolog.audio.AudioFileWriter
import com.baris.audiolog.navigation.MyAppNavHost
import com.baris.audiolog.audio.Recorder
import com.baris.audiolog.preferences.SettingsManager
import com.baris.audiolog.ui.theme.AudioLogTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var recorder: Recorder
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the output file for saving the audio data
        val audioFileWriter = AudioFileWriter(
            context = this, // Use the activity context
            outputDirectory = getExternalFilesDir(null) ?: throw IOException("Failed to get external files directory")
        )
        recorder = Recorder(this, audioFileWriter)
        settingsManager = SettingsManager(this)

        setContent {
            AudioLogTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    MyAppNavHost(
                        context =  this,
                        recorder = recorder,
                        settingsManager = settingsManager,
                        audioFileWriter = audioFileWriter
                    )


                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // When the activity is stopped, stop and release resources
        recorder.stop()
        recorder.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure the recorder resources are released when the activity is destroyed
        recorder.release()
    }


}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(onPermissionGranted: @Composable () -> Unit) {
    val recordAudioPermission = android.Manifest.permission.RECORD_AUDIO
    val writeStoragePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE // Optional for modern Android versions.

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(recordAudioPermission, writeStoragePermission)
    )

    var permissionsGranted by remember { mutableStateOf(false) }

    // Update state when permissions are granted
    LaunchedEffect(permissionState.allPermissionsGranted) {
        permissionsGranted = permissionState.allPermissionsGranted
    }

//    if (permissionsGranted) {
//        // Call the composable function when permissions are granted
//        onPermissionGranted()
//    } else {
//        // Show UI for requesting permissions
//
//    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Permissions are required to record and save audio.")
        Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
            Text("Request Permissions")
        }
        onPermissionGranted()
    }
}

