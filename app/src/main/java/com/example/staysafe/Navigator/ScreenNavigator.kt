package com.example.staysafe.Navigator

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.staysafe.View.MainScreen
import com.example.staysafe.View.RouteScreen
import com.example.staysafe.View.TrackingScreen

@Composable
fun ScreenNavigator(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            MainScreen(navController)
        }
        
        composable(Screen.Route.route) {
            RouteScreen(navController)
        }
        
        composable(Screen.Tracking.route) {
            TrackingScreen(navController)
        }
    }
}
