package com.example.staysafe.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.staysafe.map.MapScreen
import com.example.staysafe.map.PeopleScreen

@Composable
fun Navigation() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Screen.MapScreen.route
    ){
        composable(Screen.MapScreen.route) {
            MapScreen(nav)
        }
        composable(Screen.PeopleScreen.route){
            PeopleScreen(nav)
        }

    }
}