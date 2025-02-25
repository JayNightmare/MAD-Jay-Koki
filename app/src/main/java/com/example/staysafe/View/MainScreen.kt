package com.example.staysafe.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.staysafe.ViewModel.SafeViewModel
import kotlinx.coroutines.selects.select

@Composable
fun MainScreen(navController: NavController){
    val viewModel: SafeViewModel = viewModel()
    //Variable from viewModel is stored as .collectAsStateWithLifeCycle()
    //Search bar
    //val searchQuery by viewModel.searchQuery.collectAsStateWithLifeCycle()

    Column (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ){
        TextField(
            value = searchQuery, onValueChange = {viewModel.searchQuery.value = it},
            label = { ("Search the route...") },
            modifier =Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
    Footer(navController)
}
@Composable
fun Footer(navController: NavController){
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        "Home" to Pair(Icons.Default.Home,"home"),
        "Set the Route" to Pair (Icons.Default.LocationOn,"route")
    )
    NavigationBar (
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ){
        items.forEachIndexed{index, item->
            NavigationBarItem(
                icon = {Icon(item.second.first, contentDescription = item.first)},
                label = {Text(item.first)},
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.second.second)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Gray,
                    selectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}