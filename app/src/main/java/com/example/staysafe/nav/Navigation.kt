package com.example.staysafe.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.staysafe.API.Service
import com.example.staysafe.view.screens.MapScreen
import com.example.staysafe.repository.StaySafeRepository
import com.example.staysafe.viewModel.MapViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation() {
    val nav = rememberNavController()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://softwarehub.uk/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(Service::class.java)

    NavHost(
        navController = nav,
        startDestination = Screen.MapScreen.route
    ){
        composable(Screen.MapScreen.route) {
            MapScreen(
                navController = nav,
                viewModel = MapViewModel(
                    repository = StaySafeRepository(
                        service = service
                    )
                )
            )
        }
        composable(Screen.PeopleScreen.route){
//            PeopleScreen(nav)
        }

    }
}