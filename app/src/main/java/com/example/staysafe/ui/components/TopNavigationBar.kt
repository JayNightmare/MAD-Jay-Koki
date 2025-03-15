package com.example.staysafe.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar() {
    TopAppBar(
        title = { /* Empty title to focus on icons */ },
        navigationIcon = {
            IconButton(onClick = { /* TODO: Implement search functionality */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Implement add new marker functionality */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            titleContentColor = Color.White
        )
    )
}


