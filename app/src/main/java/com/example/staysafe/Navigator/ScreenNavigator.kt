package com.example.staysafe.Navigator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.staysafe.View.MainScreen
import com.example.staysafe.View.RouteScreen
import com.example.staysafe.View.HistoryScreen
import com.example.staysafe.View.BookNoteScreen
@Composable
fun ScreenNavigator() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(navController)
        }
        composable(Screen.RouteScreen.route) {
            RouteScreen(navController)
        }
    }
}