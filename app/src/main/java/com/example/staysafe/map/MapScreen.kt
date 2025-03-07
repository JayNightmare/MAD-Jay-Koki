package com.example.staysafe.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.staysafe.model.data.Location
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
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    val users by viewModel.users.collectAsState(emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(true) }

    val context = LocalContext.current

    var currentDeviceLat by remember { mutableStateOf(0.0) }
    var currentDeviceLon by remember { mutableStateOf(0.0) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5074, -0.1278), 10f)
    }
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        getCurrentLocation(context) { lat, lon ->
            currentDeviceLat = lat
            currentDeviceLon = lon
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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                cameraPositionState = cameraPositionState
            ) {
                users.forEach { user ->
                    if (user.userLatitude != null && user.userLongitude != null) {
                        val markerState = rememberMarkerState(position = LatLng(user.userLatitude, user.userLongitude))

                        Marker(
                            state = markerState,
                            title = "${user.userFirstname} ${user.userLastname}",
                            onClick = {
                                selectedUser = user
                                showSheet = true
                                true
                            }
                        )
                    }
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
                                coroutine.launch {
                                    user.userLatitude?.let { lat ->
                                        user.userLongitude?.let { lon ->
                                            cameraPositionState.moveToUserLocation(lat, lon)
                                        }
                                    }
                                }
                            }
                        )
                    } else {
                        val locationFlow = viewModel.getLocationForUser(selectedUser!!.userID)
                        val location by locationFlow.collectAsState(initial = null)

                        if (location != null) {
                            UserDetailsSheet(
                                user = selectedUser!!,
                                location = location!!,
                                userLat = currentDeviceLat,
                                userLon = currentDeviceLon,
                                onClose = { selectedUser = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

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

