package com.example.staysafe.view.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.staysafe.BuildConfig
import com.example.staysafe.model.data.User
import com.example.staysafe.ui.components.BottomNavigationBar
import com.example.staysafe.ui.components.TopNavigationBar
import com.example.staysafe.ui.components.UserDetailsSheet
import com.example.staysafe.ui.components.UserListSheet
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    val users by viewModel.users.collectAsState(emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(true) }

    val context = LocalContext.current
    var currentDeviceLat by remember { mutableDoubleStateOf(0.0) }
    var currentDeviceLon by remember { mutableDoubleStateOf(0.0) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5074, -0.1278), 10f)
    }
    val coroutineScope = rememberCoroutineScope()

    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
            isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { lat, lon ->
                currentDeviceLat = lat
                currentDeviceLon = lon
                coroutineScope.launch {
                    cameraPositionState.moveToUserLocation(lat, lon)
                }
            }
        } else {
            Log.d("MapScreen", "Location permission denied")
        }
    }

    // Fetch device location when screen loads
    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionLauncher.launch(android.Manifest.permission.INTERNET)

        getCurrentLocation(context) { lat, lon ->
            currentDeviceLat = lat
            currentDeviceLon = lon

            coroutineScope.launch {
                cameraPositionState.moveToUserLocation(lat, lon)
            }
        }
    }

    Scaffold(
        topBar = { TopNavigationBar() },
        bottomBar = {
            BottomNavigationBar(
                navController,
                onPeopleClicked = {
                    selectedUser = null
                    showSheet = true
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                cameraPositionState = cameraPositionState
            ) {
                // Show user markers
                users.forEach { user ->
                    if (user.userLatitude != null && user.userLongitude != null) {
                        val markerState = rememberMarkerState(
                            position = LatLng(user.userLatitude, user.userLongitude)
                        )

                        Marker(
                            state = markerState,
                            title = "${user.userFirstname} ${user.userLastname}",
                            onClick = {
                                selectedUser = user
                                showSheet = true
                                coroutineScope.launch {
                                    cameraPositionState.moveToUserLocation(
                                        user.userLatitude,
                                        user.userLongitude
                                    )
                                }
                                true
                            }
                        )
                    }
                }

                // Show current location marker
                if (currentDeviceLat != 0.0 && currentDeviceLon != 0.0) {
                    Log.d("MapScreen", "Current location: $currentDeviceLat, $currentDeviceLon")
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(currentDeviceLat, currentDeviceLon)
                        ),
                        title = "You",
                        snippet = "Current Location"
                    )
                } else {
                    Log.d("MapScreen", "Current location not available")
                }

                if (routePoints.isNotEmpty()) {
                    Log.d("MapScreen", "Drawing polyline with ${routePoints.size} points") // ✅ LOG POLYLINE
                    Polyline(
                        points = routePoints,
                        color = Color.Blue,
                        width = 10f
                    )
                }
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    if (selectedUser == null) {
                        UserListSheet(
                            users = users,
                            onUserSelected = { user ->
                                selectedUser = user
                                showSheet = true
                                coroutineScope.launch {
                                    user.userLatitude?.let { lat ->
                                        user.userLongitude?.let { lon ->
                                            cameraPositionState.moveToUserLocation(lat, lon)
                                        }
                                    }
                                }
                            }
                        )
                    } else {
                        val locationList by viewModel.fetchLocationById(selectedUser!!.userID).collectAsState(initial = emptyList())
                        val location = locationList.firstOrNull()

                        Log.d("API Key", "API KEY: ${BuildConfig.MAP_API_GOOGLE}")

                        if (location != null) {
                            UserDetailsSheet(
                                user = selectedUser!!,
                                location = location,
                                userLat = currentDeviceLat,
                                userLon = currentDeviceLon,
                                apiKey = BuildConfig.MAP_API_GOOGLE,
                                onRouteFetched = { routePoints = it },
                                onClose = { selectedUser = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocationReceived: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(location.latitude, location.longitude)
        }
    }
}

suspend fun CameraPositionState.moveToUserLocation(latitude: Double, longitude: Double) {
    animate(
        CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f)
    )
}