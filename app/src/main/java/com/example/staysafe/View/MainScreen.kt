package com.example.staysafe.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staysafe.ViewModel.SafeViewModel
import com.example.staysafe.ui.components.*
import com.example.staysafe.Navigator.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val viewModel: SafeViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val tripStatus by viewModel.tripStatus.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            StaySafeTopBar(title = "StaySafe")
        },
        bottomBar = {
            Footer(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Search Bar
            StaySafeTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = "Search routes or locations",
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    text = "New Trip",
                    onClick = { navController.navigate(Screen.Route.route) }
                )
                QuickActionButton(
                    icon = Icons.Default.Person,
                    text = "Contacts",
                    onClick = { /* TODO: Navigate to contacts */ }
                )
                QuickActionButton(
                    icon = Icons.Default.History,
                    text = "History",
                    onClick = { /* TODO: Navigate to history */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Contacts Section
            Text(
                "Emergency Contacts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    ContactCard(
                        name = contact.name,
                        phone = contact.phone,
                        onCall = { /* TODO: Implement call functionality */ }
                    )
                }
            }

            // Emergency Button at the bottom
            EmergencyButton(
                onClick = { /* TODO: Implement emergency action */ },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Show error dialog if there's an error message
        errorMessage?.let { message ->
            ErrorDialog(
                message = message,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(icon, contentDescription = text)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text)
        }
    }
}

@Composable
private fun ContactCard(
    name: String,
    phone: String,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onCall) {
                Icon(Icons.Default.Phone, "Call contact")
            }
        }
    }
}

@Composable
fun Footer(navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        "Home" to Pair(Icons.Default.Home, Screen.Home.route),
        "Set Route" to Pair(Icons.Default.LocationOn, Screen.Route.route)
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.second.first, contentDescription = item.first) },
                label = { Text(item.first) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.second.second) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
