package com.example.staysafe.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    LaunchedEffect(loggedInUser) {
        loggedInUser?.let {
            viewModel.fetchUserContacts(userId = it.userID)
            Log.d("UserListSheet", it.userID.toString())
        }
        Log.d("UserListSheet", "Logged in User ID: $loggedInUser")
        Log.d("UserListSheet", "Contacts: ${viewModel.contacts.value}")
    }

    LaunchedEffect(viewModel.contacts.collectAsState().value) {
        viewModel.contacts.value.forEach { contact ->
            viewModel.fetchLatestActivityForUsers(contact.userID)
        }
    }

    // Observe the user list
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    Log.d("UserListSheet", "Contacts: $contacts")

    val latestActivities by viewModel.latestActivities.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp) // Set max height to enable scrolling
            .padding(16.dp)
    ) {
        Column {
            Row {
                Text(
                    "Your Friends",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Status",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 5.dp, end = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (contacts.isEmpty()) {
                Text(
                    "No contacts found",
                    color = Color.White
                )  // Debugging: Display when user list is empty
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(contacts) { user ->
                        val userActivity = latestActivities[user.userID]

                        UserListItem(
                            user,
                            onClick = { onUserSelected(user) },
                            statusName = userActivity?.activityStatusName ?: "Unknown"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: User, statusName: String, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(12.dp)) {
        // White icon
        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(user.userFirstname, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Tap to see location", color = Color.White)
        }
        Spacer(modifier = Modifier.weight(1f))
//        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
        StatusIcon(statusName)
    }
}

