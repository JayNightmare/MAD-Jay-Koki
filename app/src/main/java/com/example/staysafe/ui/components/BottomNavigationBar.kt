package com.example.staysafe.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onPeopleClicked: () -> Unit,
    onCallClicked: () -> Unit,
    userId: Long
) {
    NavigationBar(
        containerColor = Color.Black,
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Face, contentDescription = "People", tint = Color.White) },
            label = { Text("People", color = Color.White) },
            selected = false,
            onClick = { onPeopleClicked() }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Call, contentDescription = "Call", tint = Color.White) },
            label = { Text("Call", color = Color.White) },
            selected = false,
            onClick = { onCallClicked() }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color.White
                )
            },
            label = { Text("Profile", color = Color.White) },
            selected = false,
            onClick = { /* TODO: Navigate to Profile Screen */ }
        )
    }
}
