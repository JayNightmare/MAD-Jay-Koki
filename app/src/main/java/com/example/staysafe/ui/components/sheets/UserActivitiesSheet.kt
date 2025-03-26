package com.example.staysafe.ui.components.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.model.data.Activity
import com.example.staysafe.viewModel.MapViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserActivitiesSheet(
    viewModel: MapViewModel,
    onClose: () -> Unit,
    onActivitySelected: (Activity) -> Unit
) {
    val userActivities by viewModel.activities.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchActivitiesForUser(loggedInUser?.userID ?: 0)
    }

    // Filter activities based on status and user ID
    val filteredActivities = remember(userActivities, selectedFilter, loggedInUser) {
        if (loggedInUser == null) {
            emptyList()
        } else {
            val userSpecificActivities = userActivities.filter { it.activityUserID == loggedInUser!!.userID }
            if (selectedFilter == "All") {
                userSpecificActivities.sortedByDescending { it.activityLeave }
            } else {
                userSpecificActivities.filter { it.activityStatusName == selectedFilter }
                    .sortedByDescending { it.activityLeave }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with filter
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Activities",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row {
                // Filter button
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        containerColor = Color.Black
                    ) {
                        listOf("All", "Planned", "Started", "Paused", "Cancelled", "Completed").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, color = Color.White) },
                                onClick = {
                                    selectedFilter = status
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        // Filter indicator
        if (selectedFilter != "All") {
            Text(
                "Filtered by: $selectedFilter",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (loggedInUser == null) {
            Text("Please log in to view your activities.", color = Color.Gray)
        } else if (filteredActivities.isNotEmpty()) {
            filteredActivities.forEach { activity ->
                UserActivityItem(
                    activity = activity,
                    onClick = { onActivitySelected(activity) }
                )
            }
        } else {
            Text(
                if (selectedFilter == "All") "No activities found." else "No $selectedFilter activities found.",
                color = Color.Gray
            )
        }
    }
}

@Composable
fun UserActivityItem(
    activity: Activity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    activity.activityName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    activity.activityStatusName,
                    color = when (activity.activityStatusName) {
                        "Planned" -> Color.White
                        "Started" -> Color.Yellow
                        "Paused" -> Color.Gray
                        "Cancelled" -> Color.Red
                        "Completed" -> Color.Green
                        else -> Color.White
                    }
                )
            }
            Text("From: ${activity.activityFromName}", color = Color.White)
            Text("To: ${activity.activityToName}", color = Color.White)
            Text("Leave: ${formatDate(activity.activityLeave)}", color = Color.White)
            Text("Arrive: ${formatDate(activity.activityArrive)}", color = Color.White)
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
} 