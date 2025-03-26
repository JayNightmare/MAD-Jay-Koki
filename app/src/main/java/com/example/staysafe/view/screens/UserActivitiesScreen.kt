package com.example.staysafe.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.staysafe.model.data.Activity
import com.example.staysafe.ui.components.StatusIcon
import com.example.staysafe.ui.components.forms.DateTimePicker
import com.example.staysafe.viewModel.MapViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserActivitiesScreen(
    navController: NavController,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val activities by viewModel.activities.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Fetch activities when screen is loaded
    LaunchedEffect(loggedInUser) {
        loggedInUser?.let { user ->
            viewModel.fetchActivitiesForUser(user.userID)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Activities", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Activity", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(activities) { activity ->
                    ActivityCard(
                        activity = activity,
                        onEdit = {
                            selectedActivity = activity
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedActivity = activity
                            showDeleteDialog = true
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // ! Add Activity Dialog
    if (showAddDialog) {
        ActivityDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, fromName, toName, fromAddress, toAddress, description, fromTime, toTime, fromPostcode, toPostcode ->
                viewModel.addActivity(
                    name = name,
                    fromActivityName = fromName,
                    toActivityName = toName, 
                    startAddressLine = fromAddress,
                    destAddressLine = toAddress,
                    description = description,
                    fromISOTime = fromTime,
                    toisoTime = toTime,
                    fromPostcode = fromPostcode,
                    toPostcode = toPostcode
                )
                showAddDialog = false
            },
            viewModel = viewModel
        )
    }

    // Edit Activity Dialog
    if (showEditDialog && selectedActivity != null) {
        ActivityDialog(
            activity = selectedActivity,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, fromName, toName, fromAddress, toAddress, description, fromTime, toTime, fromPostcode, toPostcode ->
                selectedActivity?.let { existingActivity ->
                    // Create updated activity with new values
                    val updatedActivity = existingActivity.copy(
                        activityName = name,
                        activityFromName = fromName,
                        activityToName = toName,
                        activityDescription = description,
                        activityLeave = fromTime,
                        activityArrive = toTime
                    )
                    
                    // Update the activity in the repository
                    viewModel.updateActivity(existingActivity.activityID, updatedActivity)
                }
                showEditDialog = false
            },
            viewModel = viewModel
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedActivity != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Activity") },
            text = { Text("Are you sure you want to delete this activity?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedActivity?.let {
                            viewModel.deleteActivity(it.activityID)
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.Black,
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }
}

@Composable
private fun ActivityCard(
    activity: Activity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: MapViewModel
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.activityName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    // Status Change Button
                    Box {
                        IconButton(onClick = { showStatusMenu = true }) {
                            StatusIcon(activity.activityStatusName)
                        }
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false },
                            modifier = Modifier.background(Color.Black)
                        ) {
                            listOf("Planned", "Started", "Paused", "Cancelled", "Completed").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, color = Color.White) },
                                    onClick = {
                                        viewModel.updateActivityStatus(activity.activityID, status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("From: ${activity.activityFromName}", color = Color.Gray, fontSize = 14.sp)
                Text("To: ${activity.activityToName}", color = Color.Gray, fontSize = 14.sp)
                Text("Status: ${activity.activityStatusName}", color = Color.Gray, fontSize = 14.sp)
                Text("Leave: ${formatDate(activity.activityLeave)}", color = Color.Gray, fontSize = 14.sp)
                Text("Arrive: ${formatDate(activity.activityArrive)}", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ActivityDialog(
    activity: Activity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(activity?.activityName ?: "") }
    var fromName by remember { mutableStateOf(activity?.activityFromName ?: "") }
    var toName by remember { mutableStateOf(activity?.activityToName ?: "") }
    var fromAddress by remember { mutableStateOf("") }
    var toAddress by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(activity?.activityDescription ?: "") }
    var fromDate by remember { mutableStateOf("") }
    var fromTime by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var toTime by remember { mutableStateOf("") }
    var fromPostcode by remember { mutableStateOf("") }
    var toPostcode by remember { mutableStateOf("") }

    // Fetch location postcodes if editing an existing activity
    LaunchedEffect(activity) {
        activity?.let { existingActivity ->
            try {
                viewModel.fetchLocationById(existingActivity.activityFromID).collect { fromLocation ->
                    fromLocation?.let {
                        fromAddress = it.locationAddress ?: ""
                        fromPostcode = it.locationPostcode ?: ""
                    }
                }
                
                viewModel.fetchLocationById(existingActivity.activityToID).collect { toLocation ->
                    toLocation?.let {
                        toAddress = it.locationAddress ?: ""
                        toPostcode = it.locationPostcode ?: ""
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error fetching location details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Initialize date and time from activity if editing
    LaunchedEffect(activity) {
        activity?.let { existingActivity ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                
                // Parse and format leave date
                val leaveDate = inputFormat.parse(existingActivity.activityLeave)
                leaveDate?.let {
                    fromDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                    fromTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                }
                
                // Parse and format arrive date
                val arriveDate = inputFormat.parse(existingActivity.activityArrive)
                arriveDate?.let {
                    toDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                    toTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Only show error toast if we're actually editing an activity
                if (activity != null) {
                    Toast.makeText(context, "Error loading activity dates", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (activity == null) "Add Activity" else "Edit Activity",
                color = Color.White
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Activity Details",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Activity Name", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF333333)
                )
                
                Text(
                    "Origin Location",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = fromName,
                    onValueChange = { fromName = it },
                    label = { Text("From Name", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )

                OutlinedTextField(
                    value = fromAddress,
                    onValueChange = { fromAddress = it },
                    label = { Text("From Address", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )
                
                OutlinedTextField(
                    value = fromPostcode,
                    onValueChange = { fromPostcode = it },
                    label = { Text("From Postcode", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DateTimePicker("From Date", fromDate) { fromDate = it }
                    Spacer(modifier = Modifier.width(8.dp))
                    DateTimePicker("From Time", fromTime, isTimePicker = true) { fromTime = it }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF333333)
                )
                
                Text(
                    "Destination Location",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = toName,
                    onValueChange = { toName = it },
                    label = { Text("Destination Name", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )

                OutlinedTextField(
                    value = toAddress,
                    onValueChange = { toAddress = it },
                    label = { Text("Destination Address", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )
                
                OutlinedTextField(
                    value = toPostcode,
                    onValueChange = { toPostcode = it },
                    label = { Text("Destination Postcode", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                    )
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DateTimePicker("To Date", toDate) { toDate = it }
                    Spacer(modifier = Modifier.width(8.dp))
                    DateTimePicker("To Time", toTime, isTimePicker = true) { toTime = it }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValidDateTime(fromDate, fromTime, toDate, toTime)) {
                        onConfirm(
                            name,
                            fromName,
                            toName,
                            fromAddress,
                            toAddress,
                            description,
                            convertToISO8601(fromDate, fromTime),
                            convertToISO8601(toDate, toTime),
                            fromPostcode,
                            toPostcode
                        )
                    } else {
                        Toast.makeText(context, "Invalid date/time. Please select future values.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(if (activity == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Color.Black
    )
}

private fun isValidDateTime(fromDate: String, fromTime: String, toDate: String, toTime: String): Boolean {
    if (fromDate.isEmpty() || fromTime.isEmpty() || toDate.isEmpty() || toTime.isEmpty()) return false

    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val from = format.parse("$fromDate $fromTime") ?: return false
    val to = format.parse("$toDate $toTime") ?: return false

    return from.before(to) && from.after(Date()) // Ensure future time
}

private fun convertToISO8601(date: String, time: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    val parsedDate = inputFormat.parse("$date $time") ?: return ""
    return outputFormat.format(parsedDate)
}

private fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        return outputFormat.format(date ?: "")
    } catch (e: Exception) {
        e.printStackTrace()
        isoDate
    }
} 