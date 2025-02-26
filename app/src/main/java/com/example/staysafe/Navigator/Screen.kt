package com.example.staysafe.Navigator

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Route : Screen("route")
    object Tracking : Screen("tracking")

    companion object {
        fun fromRoute(route: String): Screen {
            return when (route) {
                "home" -> Home
                "route" -> Route
                "tracking" -> Tracking
                else -> Home
            }
        }
    }
}
