package com.example.staysafe.nav

sealed class Screen(val route:String) {
    object MapScreen: Screen("map")
    object PeopleScreen: Screen("people")
    //For contact and call screen added later
}