package com.example.staysafe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.model.data.*

@Composable
fun UserDetailsSheet(
    user: User,
    location: Location,
    userLat: Double,
    userLon: Double,
    onClose: () -> Unit
) {
    val distance = calculateDistance(userLat, userLon, location.locationLatitude, location.locationLongitude)

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
            DirectionsButton(distance)
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
fun DirectionsButton(distance: Double) {
    val formattedDistance = "%.2f".format(distance * 0.621371)
    Button(onClick = { /* Open directions */ }) {
        Icon(Icons.Default.Create, contentDescription = "Directions")
        Spacer(modifier = Modifier.width(8.dp))
        Text("$formattedDistance miles • ~5 min")
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

fun calculateDistance(
    startLat: Double, startLng: Double,
    endLat: Double, endLng: Double
): Double {
    val earthRadius = 6371.0  // km
    val dLat = Math.toRadians(endLat - startLat)
    val dLng = Math.toRadians(endLng - startLng)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(startLat)) *
            Math.cos(Math.toRadians(endLat)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c
}
