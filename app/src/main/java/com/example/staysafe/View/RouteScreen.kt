package com.example.staysafe.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staysafe.Navigator.Screen
import com.example.staysafe.ViewModel.Activity
import com.example.staysafe.ViewModel.Contact
import com.example.staysafe.ViewModel.SafeViewModel
import com.example.staysafe.ui.components.*
import com.google.android.gms.maps.model.LatLng
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(navController: NavController) {
    val viewModel: SafeViewModel = viewModel()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var departure by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var eta by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

    Scaffold(
        topBar = {
            StaySafeTopBar(
                title = "Plan Your Route",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Route Details Section
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "Route Details",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            StaySafeTextField(
                                value = departure,
                                onValueChange = { departure = it },
                                label = "Departure Location",
                                leadingIcon = { Icon(Icons.Default.LocationOn, "Departure") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            StaySafeTextField(
                                value = destination,
                                onValueChange = { destination = it },
                                label = "Destination",
                                leadingIcon = { Icon(Icons.Default.LocationOn, "Destination") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // ETA Selection
                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Schedule, "Time")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (eta.isNotEmpty()) "ETA: $eta"
                                    else "Select Expected Time of Arrival"
                                )
                            }
                        }
                    }
                }

                item {
                    // Emergency Contact Selection
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "Emergency Contact",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            contacts.forEach { contact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(contact.name)
                                        Text(
                                            contact.phone,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    RadioButton(
                                        selected = selectedContact == contact,
                                        onClick = { selectedContact = contact }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Start Trip Button
            Button(
                onClick = {
                    if (validateInputs()) {
                        val activity = Activity(
                            name = "Trip to $destination",
                            startLocation = LatLng(0.0, 0.0), // TODO: Get actual coordinates
                            endLocation = LatLng(0.0, 0.0), // TODO: Get actual coordinates
                            eta = eta
                        )
                        viewModel.startTrip(activity)
                        navController.navigate(Screen.Tracking.route)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, "Start Trip")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Trip")
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onConfirm = { time ->
                    selectedTime = time
                    eta = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                    showTimePicker = false
                }
            )
        }

        // Error Dialog
        errorMessage?.let { message ->
            ErrorDialog(
                message = message,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    var selectedHour by remember { mutableStateOf(LocalTime.now().hour) }
    var selectedMinute by remember { mutableStateOf(LocalTime.now().minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select ETA") },
        text = {
            TimePicker(
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                onHourChange = { selectedHour = it },
                onMinuteChange = { selectedMinute = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime.of(selectedHour, selectedMinute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Hours
        NumberPicker(
            value = selectedHour,
            onValueChange = onHourChange,
            range = 0..23
        )

        Text(":")

        // Minutes
        NumberPicker(
            value = selectedMinute,
            onValueChange = onMinuteChange,
            range = 0..59
        )
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column {
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Increase")
        }

        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Decrease")
        }
    }
}

private fun validateInputs(): Boolean {
    // TODO: Implement proper validation
    return true
}
