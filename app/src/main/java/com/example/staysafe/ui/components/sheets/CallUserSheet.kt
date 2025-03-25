package com.example.staysafe.ui.components.sheets

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.staysafe.viewModel.MapViewModel
import androidx.core.net.toUri
import com.example.staysafe.model.data.User
import com.example.staysafe.model.data.UserWithContact
import com.example.staysafe.ui.components.StatusIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallUserSheet(
    contacts: List<UserWithContact>,
    viewModel: MapViewModel,
//    onClose: () -> Unit,
//    onCallUser: (UserWithContact) -> Unit
) {
    val context = LocalContext.current

    val latestActivities by viewModel.latestActivities.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Call Friends",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (contacts.isEmpty()) {
            Text(
                "No contacts found",
                color = Color.White
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    val user = contact.toUser()
                    val userActivity = latestActivities[user.userID]

                    ContactItem(
                        contact = contact,
                        viewModel = viewModel,
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${user.userPhone}".toUri()
                            }
                            context.startActivity(intent)
                        },
                        statusName = userActivity?.activityStatusName ?: "Unknown"
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: UserWithContact,
    viewModel: MapViewModel,
    onCallClick: () -> Unit,
    statusName: String
) {
    // Fetch status for this contact
    LaunchedEffect(viewModel.contacts.collectAsState().value) {
        viewModel.contacts.value.forEach { contact ->
            val user = contact.toUser()
            viewModel.fetchLatestActivityForUsers(user.userID)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val user = contact.toUser()
                Text(
                    text = "${user.userFirstname} ${user.userLastname}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.userPhone,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Show user's status
            StatusIcon(statusName)
        }
    }
}
