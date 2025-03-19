package com.example.staysafe.view.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.staysafe.BuildConfig
import com.example.staysafe.R
import com.example.staysafe.map.CustomMarker
import com.example.staysafe.model.data.User
import com.example.staysafe.ui.components.ActivitySheet
import com.example.staysafe.ui.components.BottomNavigationBar
import com.example.staysafe.ui.components.CallUserSheet
import com.example.staysafe.ui.components.TopNavigationBar
import com.example.staysafe.ui.components.UserDetailsSheet
import com.example.staysafe.ui.components.UserListSheet
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    val contacts by viewModel.contacts.collectAsState(emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showPeopleSheet by remember { mutableStateOf(false) }
    var showCallUserDialog by remember { mutableStateOf(false) }
    var showActivitySheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var currentDeviceLat by remember { mutableDoubleStateOf(0.0) }
    var currentDeviceLon by remember { mutableDoubleStateOf(0.0) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5074, -0.1278), 10f)
    }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var showStopNavigationDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val nightMapStyle = remember {
        context.resources.openRawResource(R.raw.map_style).bufferedReader().use { it.readText() }
    }

//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    )
//    {
//            isGranted ->
//        if (isGranted) {
//            getCurrentLocation(context) { lat, lon ->
//                currentDeviceLat = lat
//                currentDeviceLon = lon
//                coroutineScope.launch {
//                    cameraPositionState.moveToUserLocation(lat, lon)
//                }
//            }
//        } else {
//            Log.d("MapScreen", "Location permission denied")
//        }
//    }

    val loggedInUser = viewModel.loggedInUser.collectAsState().value
    Log.d("Flow", "Logged in user: $loggedInUser")

    // * Fetch device location when screen loads
    LaunchedEffect(Unit) {
//        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
//        permissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
//        permissionLauncher.launch(android.Manifest.permission.INTERNET)

        if (loggedInUser != null) {
            viewModel.fetchUserContacts(userId = loggedInUser.userID)
            Log.d("Flow", "User logged in: $loggedInUser")
            currentDeviceLat = loggedInUser.userLatitude ?: 0.0
            currentDeviceLon = loggedInUser.userLongitude ?: 0.0

            coroutineScope.launch {
                cameraPositionState.moveToUserLocation(currentDeviceLat, currentDeviceLon)
            }
        } else {
            Log.d("Flow", "No user logged in: $loggedInUser")
        }
    }

    LaunchedEffect(loggedInUser) {
        if (loggedInUser != null) {
            viewModel.fetchUserContacts(userId = loggedInUser.userID)
            Log.d("Flow", "User logged in: $loggedInUser")
        } else {
            Log.d("Flow", "No user logged in: $loggedInUser")
        }
    }

    Scaffold(
        topBar = { TopNavigationBar() },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onPeopleClicked = {
                    selectedUser = null
                    showPeopleSheet = true
                    showCallUserDialog = false
                    showActivitySheet = false
                },
                onCallClicked = {
                    showPeopleSheet = true
                    showCallUserDialog = true
                    showActivitySheet = false
                },
                userId = loggedInUser?.userID ?: 0
            )
        },
        floatingActionButton = {
            if (routePoints.isNotEmpty()) {
                StopNavigationButton(onStopNavigation = { showStopNavigationDialog = true })
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = MapStyleOptions(nightMapStyle),
                    mapType = MapType.NORMAL
                ),
            ) {
                contacts.forEach { user ->
                    if (user.userLatitude != null && user.userLongitude != null) {
                        val latLng = LatLng(user.userLatitude, user.userLongitude)
                        CustomMarker(
                            imageUrl = user.userImageURL,
                            fullName = "${user.userFirstname} ${user.userLastname}",
                            location = latLng,
                            onClick = {
                                selectedUser = user
                                showPeopleSheet = true
                                coroutineScope.launch {
                                    cameraPositionState.moveToUserLocation(
                                        user.userLatitude, user.userLongitude
                                    )
                                }
                            },
                            size = 50
                        )
                    }

//                    latestPosition?.let { position ->
//                        if (position.positionLatitude != null && position.positionLongitude != null) {
//                            val latLng = LatLng(position.positionLatitude, position.positionLongitude)
//
//                            CustomMarker(
//                                imageUrl = user.userImageURL,
//                                fullName = "${user.userFirstname} ${user.userLastname}",
//                                location = latLng,
//                                onClick = {
//                                    selectedUser = user
//                                    showPeopleSheet = true
//                                    coroutineScope.launch {
//                                        cameraPositionState.moveToUserLocation(
//                                            position.positionLatitude,
//                                            position.positionLongitude
//                                        )
//                                    }
//                                },
//                                size = 50
//                            )
//                        }
//                    }
                }

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color(0xff049bec),
                        width = 10f
                    )
                }
            }

            if (showPeopleSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showPeopleSheet = false
                        showCallUserDialog = false
                    },
                    sheetState = sheetState,
                    containerColor = Color.Black
                ) {
                    if (showCallUserDialog) {
                        CallUserSheet(
                            contacts = contacts,
                            viewModel = viewModel,
                        )
                    } else {
                        if (selectedUser == null) {
                            UserListSheet(
                                viewModel = viewModel,
                                onUserSelected = { user ->
                                    selectedUser = user
                                    showPeopleSheet = true
                                    coroutineScope.launch {
                                        cameraPositionState.moveToUserLocation(
                                            user.userLatitude ?: 0.0,
                                            user.userLongitude ?: 0.0
                                        )
                                    }
                                }
                            )
                        } else {
                            val location by viewModel.fetchLocationById(selectedUser!!.userID)
                                .collectAsState(initial = null)

                            UserDetailsSheet(
                                viewModel = viewModel,
                                user = selectedUser!!,
                                location = location,
                                userLat = 0.0,
                                userLon = 0.0,
                                apiKey = BuildConfig.MAP_API_GOOGLE,
                                onClose = { selectedUser = null },
                                mapStyle = nightMapStyle,
                                onRoutePlotted = { route -> routePoints = route },

                                onActivityClicked = {
                                    showPeopleSheet = false
                                    showCallUserDialog = false
                                    showActivitySheet = true
                                }
                            )
                        }
                    }
                }
            }

            if (showActivitySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showActivitySheet = false },
                    sheetState = sheetState,
                    containerColor = Color.Black
                ) {
                    ActivitySheet(
                        viewModel = viewModel,
                        userId = selectedUser?.userID ?: 0,

                        onClose = {
                            showActivitySheet = false
                            showPeopleSheet = true
                        }
                    )
                }
            }
        }
    }
}

//@SuppressLint("MissingPermission")
//fun getCurrentLocation(context: Context, onLocationReceived: (Double, Double) -> Unit) {
//    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//        if (location != null) {
//            onLocationReceived(location.latitude, location.longitude)
//        }
//    }
//}

suspend fun CameraPositionState.moveToUserLocation(latitude: Double, longitude: Double) {
    animate(
        CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 12f)
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun StopNavigationButton(onStopNavigation: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
//            .padding(bottom = 100.dp), // Adjusts placement
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = onStopNavigation,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Stop Navigation",
                tint = Color.White
            )
            Text(" Stop Navigation", color = Color.White) // Added space for better layout
        }
    }
}