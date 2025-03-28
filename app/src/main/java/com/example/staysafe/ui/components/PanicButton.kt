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

@Composable
fun PanicButton(
    viewModel: MapViewModel,
    emergencyContacts: List<UserWithContact>,
    currentActivity: Activity?
) {
    var isPressed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            // Send emergency notifications to contacts labeled as "Emergency"
            emergencyContacts.filter { it.contactLabel == "Emergency" }.forEach { contact ->
                NotificationService.showEmergencyNotification(
                    context = context,
                    contact = contact,
                    currentActivity = currentActivity,
                    panicTime = System.currentTimeMillis()
                )
            }
            
            // Reset button after 3 seconds
            kotlinx.coroutines.delay(3000)
            isPressed = false
        }
    }

    Button(
        onClick = { isPressed = true },
        enabled = !isPressed,
        modifier = Modifier
            .size(120.dp)
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) Color.Gray else Color.Red
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Panic Button",
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
        }
    }
}

private fun triggerPanicAlert(context: android.content.Context, contacts: List<UserWithContact>) {
    // Send SMS to all emergency contacts
    contacts.forEach { contact ->
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:${contact.userPhone}".toUri()
            putExtra("sms_body", "EMERGENCY: Your contact has triggered the panic button. Please check on them immediately.")
        }
        context.startActivity(intent)
    }

    // Send notification to all contacts
    val notificationService = NotificationService(context)
    contacts.forEach { contact ->
        notificationService.showPanicAlertNotification(contact)
    }

    // TODO: Send location data to emergency services
    // TODO: Start recording audio/video
    // TODO: Send last known location to emergency contacts
} 