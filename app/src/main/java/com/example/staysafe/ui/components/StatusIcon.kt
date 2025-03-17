package com.example.staysafe.ui.components

import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun StatusIcon(statusName: String) {
    val icon = when (statusName) {
        "Planned" -> Icons.Default.CalendarMonth
        "Started" -> Icons.Default.AccessTime
        "Paused" -> Icons.Default.Pause
        "Cancelled" -> Icons.Default.Clear
        "Completed" -> Icons.Default.Check
        else -> Icons.AutoMirrored.Filled.Help
    }

    val color = when (statusName) {
        "Planned" -> Color.White
        "Started" -> Color.Yellow
        "Paused" -> Color.Gray
        "Cancelled" -> Color.Gray
        "Completed" -> Color.Green
        else -> Color.White
    }

    Icon(icon, contentDescription = statusName, tint = color)
}