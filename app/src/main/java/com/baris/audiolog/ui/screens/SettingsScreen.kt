package com.baris.audiolog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baris.audiolog.ui.components.AudioFormatDropdown
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChange: (Boolean) -> Unit,
    onArrowIconClicked: () -> Unit,
    onFormatChange: (Int) -> Unit,
    onClearCacheClicked: () -> Unit,
    initialSelectedFormat: Int,
    initialIsDarkThemeEnabled: Boolean
) {

    var selectedFormat by remember { mutableIntStateOf(initialSelectedFormat) }
    var isDarkThemeEnabled by remember { mutableStateOf(initialIsDarkThemeEnabled) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearCacheDialog by remember { mutableStateOf(false) }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("Are you sure you want to clear the cache?") },
            confirmButton = {
                TextButton(onClick = {
                    onClearCacheClicked()
                    scope.launch {
                        snackbarHostState.showSnackbar("Cache cleared successfully")
                    }
                    showClearCacheDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onArrowIconClicked) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Settings Screen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Enable Dark Theme")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDarkThemeEnabled,
                    onCheckedChange = { isEnabled ->
                        isDarkThemeEnabled = isEnabled
                        onThemeChange(isEnabled)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AudioFormatDropdown(
                selectedFormat = selectedFormat,
                onFormatChange = { newFormat ->
                    selectedFormat = newFormat
                    onFormatChange(newFormat)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showClearCacheDialog = true }) {
                Text("Clear Cache")
            }
        }
    }
}