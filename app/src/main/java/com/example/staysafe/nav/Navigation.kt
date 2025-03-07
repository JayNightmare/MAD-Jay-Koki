package com.example.staysafe.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.staysafe.map.MapScreen
//import com.example.staysafe.map.PeopleScreen
import com.example.staysafe.model.database.StaySafeDatabase
import com.example.staysafe.viewModel.MapViewModel

@Composable
fun Navigation(database: StaySafeDatabase) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Screen.MapScreen.route
    ){
        composable(Screen.MapScreen.route) {
            MapScreen(
                navController = nav,
                viewModel = MapViewModel(
                    userDao = database.userDao(),
                    locationDao = database.locationDao()
                )
            )
        }
        composable(Screen.PeopleScreen.route){
//            PeopleScreen(nav)
        }

    }
}