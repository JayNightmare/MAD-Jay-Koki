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
fun UserDetailsSheet(user: User, location: Location, onClose: () -> Unit) {
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
//            DirectionsButton(user.distance)
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
    Button(onClick = { /* Open directions */ }) {
        Icon(Icons.Default.Create, contentDescription = "Directions")
        Spacer(modifier = Modifier.width(8.dp))
        Text("$distance miles • 5 min")
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
