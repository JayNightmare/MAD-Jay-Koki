package com.example.staysafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.staysafe.model.data.UserWithContact
import com.example.staysafe.viewModel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    navController: NavController,
    viewModel: MapViewModel,
    onUserSelected: (UserWithContact) -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val contacts by viewModel.contacts.collectAsState()
    val filteredContacts = contacts.filter { user ->
        val query = searchQuery.lowercase()
        user.userFirstname?.lowercase()?.contains(query) == true ||
        user.userLastname?.lowercase()?.contains(query) == true ||
        user.userUsername?.lowercase()?.contains(query) == true ||
        user.userPhone?.lowercase()?.contains(query) == true
    }

    Column {
        TopAppBar(
            title = {
                if (showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search contacts...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Gray,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            navigationIcon = {
                if (showSearch) {
                    IconButton(onClick = { 
                        showSearch = false
                        searchQuery = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
                    }
                } else {
                    IconButton(onClick = { showSearch = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
            },
            actions = {
                IconButton(onClick = { navController.navigate("add") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
                titleContentColor = Color.White
            )
        )

        if (showSearch && searchQuery.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .zIndex(1f),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(filteredContacts) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .clickable {
                                    onUserSelected(user)
                                    showSearch = false
                                    searchQuery = ""
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "${user.userFirstname} ${user.userLastname}",
                                    color = Color.White
                                )
                                Text(
                                    text = user.userPhone,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


