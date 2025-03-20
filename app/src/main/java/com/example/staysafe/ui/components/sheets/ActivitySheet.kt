package com.example.staysafe.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.model.data.Activity
import com.example.staysafe.viewModel.MapViewModel

@Composable
fun ActivitySheet(
    viewModel: MapViewModel,
    userId: Long,
    onClose: () -> Unit
) {
    val userActivities by viewModel.activities.collectAsState()

    // Fetch all activities for the user when sheet opens
    LaunchedEffect(userId) {
        viewModel.fetchActivitiesForUser(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                "User Activities",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        if (userActivities.isNotEmpty()) {
            userActivities.forEach { activity ->
                ActivityItem(activity)
            }
        } else {
            Text("No activities found for this user.", color = Color.Gray)
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(activity.activityName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("From: ${activity.activityFromName}", color = Color.White)
            Text("To: ${activity.activityToName}", color = Color.White)
            Text("Leave: ${activity.activityLeave}", color = Color.White)
            Text("Arrive: ${activity.activityArrive}", color = Color.White)
            Text("Status: ${activity.activityStatusName}", color = Color.White)
        }
    }
}
