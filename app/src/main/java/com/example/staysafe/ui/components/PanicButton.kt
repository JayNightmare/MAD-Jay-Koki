package com.example.staysafe.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.staysafe.model.data.UserWithContact
import com.example.staysafe.service.NotificationService
import androidx.core.net.toUri
import com.example.staysafe.model.data.Activity
import com.example.staysafe.viewModel.MapViewModel
import com.example.staysafe.nav.Screen
import com.example.staysafe.service.CameraService
import kotlinx.coroutines.launch

@Composable
fun PanicButton(
    viewModel: MapViewModel,
    emergencyContacts: List<UserWithContact>,
    currentActivity: Activity?
) {
    val context = LocalContext.current
    val cameraService = remember { CameraService(context) }
    val coroutineScope = rememberCoroutineScope()

    FloatingActionButton(
        onClick = {
            coroutineScope.launch {
                try {
                    // Capture photo
                    val photoUri = cameraService.capturePhoto()
                    
                    // Filter emergency contacts
                    val emergencyContacts = emergencyContacts.filter { it.contactLabel == "Emergency" }
                    
                    // Get current user's location and activity
                    val loggedInUser = viewModel.loggedInUser.value
                    if (loggedInUser != null) {
                        val emergencyData = mapOf(
                            "type" to "emergency",
                            "userId" to loggedInUser.userID.toString(),
                            "panicTime" to System.currentTimeMillis().toString(),
                            "userName" to "${loggedInUser.userFirstname} ${loggedInUser.userLastname}",
                            "activityName" to (currentActivity?.activityName ?: "No current activity"),
                            "latitude" to (loggedInUser.userLatitude?.toString() ?: ""),
                            "longitude" to (loggedInUser.userLongitude?.toString() ?: ""),
                            "activityId" to (currentActivity?.activityID?.toString() ?: ""),
                            "photoUri" to (photoUri?.toString() ?: "")
                        )

                        // Send emergency notification to all emergency contacts
                        emergencyContacts.forEach { contact ->
                            viewModel.sendEmergencyAlert(contact.userID, emergencyData)
                        }

                        // Show local notification
                        NotificationService.showEmergencyNotification(
                            context = context,
                            userId = loggedInUser.userID,
                            userName = "${loggedInUser.userFirstname} ${loggedInUser.userLastname}",
                            currentActivity = currentActivity,
                            photoUri = photoUri
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        containerColor = Color.Red,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Panic Button",
            tint = Color.White
        )
    }
}

