package com.example.staysafe.Navigator

sealed class Screen(val route:String) {
    object MainScreen :Screen("home")
    object RouteScreen: Screen("route")

}