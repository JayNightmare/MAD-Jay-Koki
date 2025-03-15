package com.example.staysafe.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.User
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserDetailsSheet(
    viewModel: MapViewModel,
    user: User,
    location: Location?,
    userLat: Double,
    userLon: Double,
    apiKey: String,
    onRoutePlotted: (List<LatLng>) -> Unit,
    onClose: () -> Unit
) {
    var distance by remember { mutableStateOf("Calculating...") }
    var duration by remember { mutableStateOf("Calculating...") }

//    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userLat, userLon, location, apiKey) {
        if (location != null) {
            val result = viewModel.fetchDistanceAndDuration(
                user = user,
                originLat = userLat,
                originLng = userLon,
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
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(user.userFirstname, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Text(text = "User Information", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        Text(text = "Username: ${user.userUsername}", color = Color.White)
        Text(text = "Phone: ${user.userPhone}", color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))

        if (location != null) {
            Text(text = "Destination", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            Text(text = "Location: ${location.locationName ?: "Unknown"}", color = Color.White)
            Text(text = "Address: ${location.locationAddress ?: "No address available"}", color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Estimated Travel Time", fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "$distance • $duration", color = Color.White)
        } else {
            Text(text = "No planned destination", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            ContactButton()
            Spacer(modifier = Modifier.width(12.dp))
            DirectionsButton(
                viewModel = viewModel,
                userLat = userLat,
                userLon = userLon,
                friendLat = user.userLatitude!!,
                friendLon = user.userLongitude!!,
                apiKey = apiKey,
                onRoutePlotted = onRoutePlotted
            )
        }
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

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DirectionsButton(
    viewModel: MapViewModel,
    userLat: Double,
    userLon: Double,
    friendLat: Double,
    friendLon: Double,
    apiKey: String,
    onRoutePlotted: (List<LatLng>) -> Unit // Sends route back to MapScreen
) {
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        Log.d("DirectionsButton", "Fetching Route to Friend...")
        coroutineScope.launch {
            viewModel.fetchRoute(
                start = LatLng(userLat, userLon),
                end = LatLng(friendLat, friendLon),
                apiKey = apiKey,
                onResult = { routePoints ->
                    Log.d("DirectionsButton", "Route fetched: ${routePoints.size} points")
                    onRoutePlotted(routePoints)
                }
            )
        }
    }) {
        Icon(Icons.Default.Create, contentDescription = "Directions")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Get Directions")
    }
}
