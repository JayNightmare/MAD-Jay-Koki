package com.example.staysafe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import com.example.staysafe.map.fetchDistanceAndDuration
import com.example.staysafe.model.data.*

@Composable
fun UserDetailsSheet(
    user: User,
    location: Location,
    userLat: Double,
    userLon: Double,
    apiKey: String,
    onClose: () -> Unit
) {
    var distance by remember { mutableStateOf("Calculating...") }
    var duration by remember { mutableStateOf("Calculating...") }

    Log.d("API Key", apiKey)

    LaunchedEffect(userLat, userLon, location, apiKey) {
        val result = fetchDistanceAndDuration(
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

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(user.userFirstname, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Text("${location.locationName}\nLive • Last Seen")

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            ContactButton()
            DirectionsButton(distance, duration)
        }

        Spacer(modifier = Modifier.height(12.dp))

        NotificationsSection()
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

@Composable
fun DirectionsButton(distance: String, duration: String) {
    Button(onClick = { /* Open directions */ }) {
        Icon(Icons.Default.Create, contentDescription = "Directions")
        Spacer(modifier = Modifier.width(8.dp))
        Text("$distance • $duration")
    }
}


@Composable
fun NotificationsSection() {
    Column {
        Text("Notifications", fontWeight = FontWeight.Bold)
        TextButton(onClick = { /* Add notifications */ }) {
            Text("Add")
        }
    }
}
