package com.example.staysafe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.viewModel.MapViewModel

@Composable
fun ActivitySheet(
    viewModel: MapViewModel,
    userId: Long,
    onClose: () -> Unit // Close when done
) {
    val latestActivity by viewModel.latestActivityForUser.collectAsState()

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
                "Activity Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        if (latestActivity != null) {
            Text("Name: ${latestActivity?.activityName}", color = Color.White)
            Text("Description: ${latestActivity?.activityDescription}", color = Color.White)
            Text("From: ${latestActivity?.activityFromName}", color = Color.White)
            Text("To: ${latestActivity?.activityToName}", color = Color.White)
            Text("Arrive: ${latestActivity?.activityArrive}", color = Color.White)
            Text("Status: ${latestActivity?.activityStatusName}", color = Color.White)
        } else {
            Text("No activity data available.", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
