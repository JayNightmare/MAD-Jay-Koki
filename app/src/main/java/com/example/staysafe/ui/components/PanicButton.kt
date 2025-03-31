package com.example.staysafe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.staysafe.model.data.UserWithContact
import com.example.staysafe.service.NotificationService
import com.example.staysafe.model.data.Activity
import com.example.staysafe.viewModel.MapViewModel
import kotlinx.coroutines.launch

@Composable
fun PanicButton(
    viewModel: MapViewModel,
    emergencyContacts: List<UserWithContact>,
    currentActivity: Activity?,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    FloatingActionButton(
        onClick = {
            coroutineScope.launch {
                try {
                    // Filter emergency contacts
                    val listOfEmergencyContacts = emergencyContacts.filter { it.contactLabel == "Emergency" }
                    
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
                        )

                        // Send emergency notification to all emergency contacts
                        listOfEmergencyContacts.forEach { contact ->
                            viewModel.sendEmergencyAlert(contact.userID, emergencyData)
                        }

                        // Show local notification
                        NotificationService.showEmergencyNotification(
                            context = context,
                            userId = loggedInUser.userID,
                            userName = "${loggedInUser.userFirstname} ${loggedInUser.userLastname}",
                            currentActivity = currentActivity,
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

