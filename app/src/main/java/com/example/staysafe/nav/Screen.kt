package com.example.staysafe.nav

sealed class Screen(val route:String) {
    object MapScreen: Screen("map")
    object LoginScreen: Screen("login")
    object RegisterUserScreen: Screen("register")
    object AddScreen: Screen("add")
    object ProfileScreen: Screen("profile")
    object SettingsScreen: Screen("settings")
}
