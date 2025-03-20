package com.example.staysafe.nav

sealed class Screen(val route:String) {
    object MapScreen: Screen("map")
    object LoginScreen: Screen("login")
    object RegisterUserScreen: Screen("register")
    object AddUserScreen: Screen("addUsers")
}
