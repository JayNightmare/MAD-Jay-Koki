package com.example.staysafe.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.User
import com.example.staysafe.ui.components.BottomNavigationBar
import com.example.staysafe.ui.components.TopNavigationBar
import com.example.staysafe.ui.components.UserDetailsSheet
import com.example.staysafe.ui.components.UserListSheet
import com.example.staysafe.viewModel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, viewModel: MapViewModel) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    val users by viewModel.users.collectAsState(emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopNavigationBar() },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(51.5074, -0.1278), 10f)
                }
            )

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    if (selectedUser == null) {
                        UserListSheet(users = users, onUserSelected = { user ->
                            selectedUser = user
                        })
                    } else {
                        val locationFlow = viewModel.getLocationForUser(selectedUser!!.userID)
                        val location by locationFlow.collectAsState(initial = null)

                        if (location != null) {
                            UserDetailsSheet(
                                user = selectedUser!!,
                                location = location!!,
                                onClose = { selectedUser = null }
                            )
                        }
                    }
                }
            }
        }
    }
}
