package com.example.staysafe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.staysafe.model.data.*
import com.example.staysafe.viewModel.MapViewModel

@Composable
fun UserListSheet(
    viewModel: MapViewModel,
    onUserSelected: (User) -> Unit
) {
    // Ensure data fetching starts when the screen loads
    LaunchedEffect(Unit) {
        println("DEBUG: Calling fetchAllData() in UserListSheet")
        viewModel.fetchAllData()
    }

    // Observe the user list
    val users by viewModel.users.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp) // Set max height to enable scrolling
            .padding(16.dp)
    ) {
        Column {
            Text("Nearby Users", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            if (users.isEmpty()) {
                Text("No users found")  // Debugging: Display when user list is empty
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(users) { user ->
                        UserListItem(user, onClick = { onUserSelected(user) })
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: User, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp)) {
        Icon(Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(user.userFirstname, fontWeight = FontWeight.Bold)
            Text("Tap to see location")
        }
    }
}

