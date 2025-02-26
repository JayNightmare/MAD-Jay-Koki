package com.example.staysafe.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staysafe.Navigator.Screen
import com.example.staysafe.ViewModel.SafeViewModel
import com.example.staysafe.ViewModel.TripStatus
import com.example.staysafe.ui.components.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(navController: NavController) {
    val viewModel: SafeViewModel = viewModel()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val tripStatus by viewModel.tripStatus.collectAsStateWithLifecycle()
    val currentActivity by viewModel.currentActivity.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showEndTripDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    // Set default camera position
    val defaultLocation = LatLng(51.5074, -0.1278) // London as default
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: defaultLocation,
            15f
        )
    }

    // Update camera position when location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    // Check trip status and navigate accordingly
    LaunchedEffect(tripStatus) {
        if (tripStatus == TripStatus.COMPLETED) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            StaySafeTopBar(
                title = currentActivity?.name ?: "Trip in Progress"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true,
                    compassEnabled = true
                )
            ) {
                // Show current location marker
                currentLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Current Location"
                    )
                }

                // Show route polyline if available
                currentActivity?.let { activity ->
                    Polyline(
                        points = listOf(
                            activity.startLocation,
                            activity.endLocation
                        ),
                        color = MaterialTheme.colorScheme.primary.hashCode(),
                        width = 5f
                    )
                }
            }

            // Trip Info Card
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        currentActivity?.name ?: "Trip in Progress",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "ETA: ${currentActivity?.eta ?: "Calculating..."}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // Emergency Button
                EmergencyButton(
                    onClick = { showEmergencyDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // End Trip Button
                Button(
                    onClick = { showEndTripDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Stop, "End Trip")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End Trip")
                }
            }
        }

        // End Trip Confirmation Dialog
        if (showEndTripDialog) {
            AlertDialog(
                onDismissRequest = { showEndTripDialog = false },
                title = { Text("End Trip") },
                text = { Text("Are you sure you want to end this trip?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.completeTrip()
                            showEndTripDialog = false
                        }
                    ) {
                        Text("Yes, End Trip")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndTripDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Emergency Dialog
        if (showEmergencyDialog) {
            AlertDialog(
                onDismissRequest = { showEmergencyDialog = false },
                title = { Text("Emergency Alert", color = MaterialTheme.colorScheme.error) },
                text = {
                    Column {
                        Text("Do you want to:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Alert your emergency contacts")
                        Text("• Share your current location")
                        Text("• Call emergency services")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // TODO: Implement emergency actions
                            showEmergencyDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Yes, Send Alert")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmergencyDialog = false }) {
                        Text("Cancel")
                    }
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
