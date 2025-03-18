package com.example.staysafe.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.staysafe.map.CustomMarker
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.User
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserDetailsSheet(
    viewModel: MapViewModel,
    user: User,
    location: Location?,
    userLat: Double,
    userLon: Double,
    apiKey: String,
    onClose: () -> Unit,
    mapStyle: String,
    onRoutePlotted: (List<LatLng>) -> Unit
) {
    var distance by remember { mutableStateOf("Calculating...") }
    var duration by remember { mutableStateOf("Calculating...") }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    val friendLatLng = LatLng(user.userLatitude!!, user.userLongitude!!)
    val latestActivity by viewModel.latestActivityForUser.collectAsState()

    LaunchedEffect(user.userID, user.userLatitude, user.userLongitude, location, apiKey) {
        viewModel.fetchLatestActivityForUser(user.userID)

        if (location != null) {
            val result = viewModel.fetchDistanceAndDuration(
                originLat = user.userLatitude,
                originLng = user.userLongitude,
                destLat = location.locationLatitude,
                destLng = location.locationLongitude,
                apiKey = apiKey
            )

            if (result != null) {
                distance = result.first
                duration = result.second
            } else {
                distance = "Unavailable"
                duration = "Unavailable"
            }

            viewModel.fetchRoute(
                start = friendLatLng,
                end = LatLng(location.locationLatitude, location.locationLongitude),
                apiKey = apiKey
            ) { newRoute ->
                routePoints = newRoute
                if (newRoute.isNotEmpty()) {
                    coroutineScope.launch {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngBounds(
                                LatLngBounds.builder().apply {
                                    newRoute.forEach { include(it) }
                                }.build(),
                                100 // Padding for visibility
                            )
                        )
                    }
                }
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
        // * Header
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                "${user.userFirstname} ${user.userLastname}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Text("User Information:", fontSize = 16.sp, color = Color.White)
        Text("Username: ${user.userUsername}", color = Color.White)
        Text("Phone: ${user.userPhone}", color = Color.White)

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.White, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (location != null) {
            Text("Destination:", fontSize = 16.sp, color = Color.White)
            Text("Location: ${location.locationName ?: "Unknown"}", color = Color.White)
            Text(
                "Address: ${location.locationAddress ?: "No address available"}",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Estimated Travel Time", fontSize = 16.sp, color = Color.White)
            Text("$distance • $duration", color = Color.White)
        } else {
            Text("No planned destination", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.White, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        if (location != null) {
            Text(
                "Activity Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Name: ${latestActivity?.activityName}", color = Color.White)
            Text("Description: ${latestActivity?.activityDescription}", color = Color.White)
            Text("From: ${latestActivity?.activityFromName}", color = Color.White)
            Text("To: ${latestActivity?.activityToName}", color = Color.White)
            Text("Arrive: ${latestActivity?.activityArrive}", color = Color.White)
            Text("Status: ${latestActivity?.activityStatusName}", color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Route Preview", fontSize = 16.sp, color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Use 2 fingers to interact with the map", color = Color.Gray)

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = MapStyleOptions(mapStyle)
                )
            ) {
                // Draw the route in Orange
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color(255, 165, 0),
                        width = 8f
                    )
                }

                CustomMarker(
                    imageUrl = user.userImageURL,
                    fullName = "${user.userFirstname} ${user.userLastname}",
                    location = friendLatLng,
                    onClick = {
                        Log.d(
                            "CustomMarker",
                            "Clicked on ${user.userFirstname}'s marker"
                        )
                    },
                    size = 25
                )

//                if (location != null) {
//                    Marker(
//                        state = rememberMarkerState(position = LatLng(location.locationLatitude, location.locationLongitude)),
//                        title = "Destination: ${location.locationName}"
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // * Contact and Directions Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ContactButton()
            DirectionsButton(
                viewModel = viewModel,
                userLat = userLat,
                userLon = userLon,
                friendLat = user.userLatitude,
                friendLon = user.userLongitude,
                apiKey = apiKey,
                onRoutePlotted = onRoutePlotted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.White, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ContactButton() {
    Button(onClick = { /* Open contact */ }) {
        Icon(Icons.Default.Call, contentDescription = "Contact")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Contact")
    }
}

@OptIn(UnstableApi::class)
@Composable
fun DirectionsButton(
    viewModel: MapViewModel,
    userLat: Double,
    userLon: Double,
    friendLat: Double,
    friendLon: Double,
    apiKey: String,
    onRoutePlotted: (List<LatLng>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Button(onClick = {
        Log.d("DirectionsButton", "Fetching Route to Friend...")
        coroutineScope.launch {
            viewModel.fetchRoute(
                start = LatLng(userLat, userLon),
                end = LatLng(friendLat, friendLon),
                apiKey = apiKey
            ) { routePoints ->
                Log.d("DirectionsButton", "Route fetched: ${routePoints.size} points")

                // * ✅ Update map route
                onRoutePlotted(routePoints)

                // * ✅ Open Google Maps Navigation
                val gmmIntentUri = Uri.parse("google.navigation:q=$friendLat,$friendLon")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                }
            }
        }
    }) {
        Icon(Icons.Default.Create, contentDescription = "Directions")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Get Directions")
    }
}
