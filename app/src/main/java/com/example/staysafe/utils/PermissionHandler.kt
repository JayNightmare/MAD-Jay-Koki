package com.example.staysafe.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    var showRationale by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
    }

    when {
        locationPermissionState.status.isGranted -> {
            content()
        }
        locationPermissionState.status.shouldShowRationale -> {
            showRationale = true
        }
        else -> {
            locationPermissionState.launchPermissionRequest()
        }
    }

    if (showRationale) {
        PermissionRationaleDialog(
            onConfirm = {
                showRationale = false
                locationPermissionState.launchPermissionRequest()
            },
            onDismiss = { showRationale = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            onConfirm = { showSettings = false },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission Required") },
        text = {
            Text(
                "StaySafe needs access to location to track your journey and ensure your safety. " +
                "This allows us to monitor your route and alert your emergency contacts if needed."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

@Composable
private fun SettingsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = {
            Text(
                "Location permission is required for core functionality. " +
                "Please open Settings to grant the permission."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun openAppSettings(context: Context) {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).also { intent ->
        context.startActivity(intent)
    }
}
