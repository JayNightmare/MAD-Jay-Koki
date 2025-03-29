package com.example.staysafe.view.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.staysafe.model.data.Activity
import com.example.staysafe.model.data.UserWithContact
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    navController: NavController,
    viewModel: MapViewModel,
    userId: Long,
    userName: String,
    activityId: Long? = null,
    photoUri: String? = null
) {
    val context = LocalContext.current
    var activity by remember { mutableStateOf<Activity?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var user by remember { mutableStateOf<UserWithContact?>(null) }

    // Fetch user and activity data
    LaunchedEffect(userId, activityId) {
        viewModel.getUserById(userId)?.let { userData ->
            user = userData
            userLocation = userData.userLatitude?.let { lat ->
                userData.userLongitude?.let { lon ->
                    LatLng(lat, lon)
                }
            }
        }

        activityId?.let { id ->
            viewModel.activities.value.find { it.activityID == id }?.let { activityData ->
                activity = activityData
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Alert") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Red
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emergency Photo
            photoUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        model = uri.toUri(),
                        contentDescription = "Emergency Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // User Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "User: $userName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    activity?.let {
                        Text(
                            text = "Activity: ${it.activityName}",
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Time: ${SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date())}",
                        fontSize = 16.sp
                    )
                }
            }

            // Map
            userLocation?.let { location ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(location, 15f)
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = location),
                            title = userName
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        userLocation?.let { location ->
                            val uri = "google.navigation:q=${location.latitude},${location.longitude}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Navigate")
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:911")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Icon(Icons.Default.Emergency, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call 911")
                }
            }
        }
    }
} 