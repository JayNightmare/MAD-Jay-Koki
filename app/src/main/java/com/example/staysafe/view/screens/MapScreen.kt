package com.example.staysafe.view.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.staysafe.BuildConfig
import com.example.staysafe.R
import com.example.staysafe.map.CustomMarker
import com.example.staysafe.model.data.*
import com.example.staysafe.ui.components.sheets.ActivitySheet
import com.example.staysafe.ui.components.BottomNavigationBar
import com.example.staysafe.ui.components.sheets.CallUserSheet
import com.example.staysafe.ui.components.TopNavigationBar
import com.example.staysafe.ui.components.sheets.UserActivitiesSheet
import com.example.staysafe.ui.components.sheets.UserDetailsSheet
import com.example.staysafe.ui.components.sheets.UserListSheet
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
    var selectedUser by remember { mutableStateOf<UserWithContact?>(null) }
    val contacts by viewModel.contacts.collectAsState(emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }
    var showStatusMenu by remember { mutableStateOf(false) }
    val activities by viewModel.activities.collectAsState()

    // Update selectedActivity when activities change
    LaunchedEffect(activities, selectedActivity) {
        selectedActivity?.let { currentActivity ->
            val updatedActivity = activities.find { it.activityID == currentActivity.activityID }
            if (updatedActivity != null) {
                selectedActivity = updatedActivity
            }
        }
    }

    var showPeopleSheet by remember { mutableStateOf(false) }
    var showCallUserDialog by remember { mutableStateOf(false) }
    var showActivitySheet by remember { mutableStateOf(false) }
    var showUserActivitiesSheet by remember { mutableStateOf(false) }

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

    val loggedInUser = viewModel.loggedInUser.collectAsState().value
    Log.d("Flow", "Logged in user: $loggedInUser")

    // * Fetch device location when screen loads
    LaunchedEffect(Unit) {
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

    // Update route when selected activity changes
    LaunchedEffect(selectedActivity) {
        selectedActivity?.let { activity ->
            viewModel.getRouteForActivity(activity) { route ->
                routePoints = route
                // Move camera to show the entire route
                if (route.isNotEmpty()) {
                    val bounds = route.fold(LatLngBounds.builder()) { builder, point ->
                        builder.include(point)
                    }.build()
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100)
                        )
                    }
                }
            }
        } ?: run {
            routePoints = emptyList()
        }
    }

    // Add state for tracking if activity is started
    var isActivityStarted by remember { mutableStateOf(false) }

    // Add state for tracking if activity is paused
    var isActivityPaused by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            TopNavigationBar(
                navController = navController,
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
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onPeopleClicked = {
                    selectedUser = null
                    selectedActivity = null
                    showPeopleSheet = true
                    showCallUserDialog = false
                    showActivitySheet = false
                    showUserActivitiesSheet = false
                },
                onCallClicked = {
                    showPeopleSheet = true
                    showCallUserDialog = true
                    showActivitySheet = false
                    showUserActivitiesSheet = false
                },
                onMyActivitiesClicked = {
                    showPeopleSheet = false
                    showCallUserDialog = false
                    showActivitySheet = false
                    showUserActivitiesSheet = true
                },
                userId = loggedInUser?.userID ?: 0
            )
        },
        floatingActionButton = {
            if (selectedActivity != null && selectedActivity?.activityUserID == loggedInUser?.userID) {
                Box {
                    FloatingActionButton(
                        onClick = { showStatusMenu = true },
                        containerColor = when (selectedActivity?.activityStatusName) {
                            "Planned" -> Color.White
                            "Started" -> Color.Yellow
                            "Paused" -> Color.Gray
                            "Cancelled" -> Color.Red
                            "Completed" -> Color.Green
                            else -> Color.White
                        }
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Change Activity Status",
                            tint = Color.Black
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        selectedActivity?.let { activity ->
                            if (activity.activityStatusName != "Planned") {
                                DropdownMenuItem(
                                    text = { Text("Planned") },
                                    onClick = {
                                        viewModel.updateActivityStatus(activity.activityID, "Planned")
                                        showStatusMenu = false
                                    }
                                )
                            }
                            if (activity.activityStatusName != "Started") {
                                DropdownMenuItem(
                                    text = { Text("Started") },
                                    onClick = {
                                        viewModel.updateActivityStatus(activity.activityID, "Started")
                                        showStatusMenu = false
                                    }
                                )
                            }
                            if (activity.activityStatusName != "Paused") {
                                DropdownMenuItem(
                                    text = { Text("Paused") },
                                    onClick = {
                                        viewModel.updateActivityStatus(activity.activityID, "Paused")
                                        showStatusMenu = false
                                    }
                                )
                            }
                            if (activity.activityStatusName != "Cancelled") {
                                DropdownMenuItem(
                                    text = { Text("Cancelled") },
                                    onClick = {
                                        viewModel.updateActivityStatus(activity.activityID, "Cancelled")
                                        showStatusMenu = false
                                    }
                                )
                            }
                            if (activity.activityStatusName != "Completed") {
                                DropdownMenuItem(
                                    text = { Text("Completed") },
                                    onClick = {
                                        viewModel.updateActivityStatus(activity.activityID, "Completed")
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Start
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
                // Draw route if available
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color(0xff049bec),
                        width = 10f
                    )
                }

                // Show markers for selected activity
                selectedActivity?.let { activity ->
                    val fromLocation = viewModel.locations.value.find { it.locationID == activity.activityFromID.toInt() }
                    val toLocation = viewModel.locations.value.find { it.locationID == activity.activityToID.toInt() }

                    fromLocation?.let { location ->
                        CustomMarker(
                            imageUrl = "start",
                            fullName = "Start: ${activity.activityFromName}",
                            location = LatLng(location.locationLatitude, location.locationLongitude),
                            onClick = { },
                            size = 50
                        )
                    }

                    toLocation?.let { location ->
                        CustomMarker(
                            imageUrl = "end",
                            fullName = "End: ${activity.activityToName}",
                            location = LatLng(location.locationLatitude, location.locationLongitude),
                            onClick = { },
                            size = 50
                        )
                    }
                }

                // Show contact markers
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
                }
            }

            // Navigation Controls
            if (selectedActivity != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                ) {
                    if (!isActivityStarted) {
                        // Start Button
                        FloatingActionButton(
                            onClick = {
                                viewModel.updateActivityStatus(selectedActivity!!.activityID, "Started")
                                isActivityStarted = true
                                isActivityPaused = false
                            },
                            containerColor = Color(0xFF4CAF50), // Green color for start
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Activity",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Row with Pause/Resume and Cancel buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .shadow(4.dp)
                        ) {
                            // Pause/Resume Button
                            FloatingActionButton(
                                onClick = {
                                    if (isActivityPaused) {
                                        viewModel.updateActivityStatus(selectedActivity!!.activityID, "Started")
                                        isActivityPaused = false
                                    } else {
                                        viewModel.updateActivityStatus(selectedActivity!!.activityID, "Paused")
                                        isActivityPaused = true
                                    }
                                },
                                containerColor = if (isActivityPaused) 
                                    Color(0xFF4CAF50) // Green color for resume
                                else 
                                    Color(0xFFFFA000), // Orange color for pause
                                modifier = Modifier.size(56.dp).padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    imageVector = if (isActivityPaused) 
                                        Icons.Default.PlayArrow
                                    else 
                                        Icons.Default.Pause,
                                    contentDescription = if (isActivityPaused) 
                                        "Resume Activity"
                                    else 
                                        "Pause Activity",
                                    tint = Color.White
                                )
                            }

                            // Cancel Button
                            FloatingActionButton(
                                onClick = {
                                    viewModel.updateActivityStatus(selectedActivity!!.activityID, "Cancelled")
                                    isActivityStarted = false
                                    isActivityPaused = false
                                    selectedActivity = null
                                    routePoints = emptyList()
                                },
                                containerColor = Color(0xFFE53935), // Red color for cancel
                                modifier = Modifier.size(56.dp).padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Activity",
                                    tint = Color.White
                                )
                            }
                        }
                    }
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
                                user = selectedUser!!.toUser(),
                                location = location,
                                userLat = currentDeviceLat,
                                userLon = currentDeviceLon,
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
                        },
                        onActivitySelected = { activity ->
                            selectedActivity = activity
                            showActivitySheet = false
                            showPeopleSheet = false
                        }
                    )
                }
            }

            if (showUserActivitiesSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showUserActivitiesSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.Black
                ) {
                    UserActivitiesSheet(
                        viewModel = viewModel,
                        onClose = { showUserActivitiesSheet = false },
                        onActivitySelected = { activity ->
                            selectedActivity = activity
                            showUserActivitiesSheet = false
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