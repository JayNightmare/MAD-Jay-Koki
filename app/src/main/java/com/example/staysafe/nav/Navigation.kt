package com.example.staysafe.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.staysafe.view.screens.MapScreen
import com.example.staysafe.model.database.StaySafeDatabase
import com.example.staysafe.viewModel.MapViewModel

@RequiresApi(Build.VERSION_CODES.O)
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