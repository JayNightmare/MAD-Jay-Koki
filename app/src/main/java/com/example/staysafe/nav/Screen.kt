package com.example.staysafe.nav

sealed class Screen(val route:String) {
    object MapScreen: Screen("map")
    object LoginScreen: Screen("login")
    object RegisterUserScreen: Screen("register")
    object AddScreen: Screen("add")
    object ProfileScreen: Screen("profile")
    object SettingsScreen: Screen("settings")
    object UserActivitiesScreen : Screen("user_activities")
    object EmergencyScreen : Screen("emergency/{userId}/{userName}/{activityId}") {
        fun createRoute(
            userId: Long,
            userName: String,
            activityId: Long?,
        ): String {
            return "emergency/$userId/$userName/${activityId ?: 0}}"
        }
    }
}
