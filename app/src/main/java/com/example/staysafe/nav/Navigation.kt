package com.example.staysafe.nav

import RegisterUserScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.*
import com.example.staysafe.API.Service
import com.example.staysafe.view.screens.*
import com.example.staysafe.repository.StaySafeRepository
import com.example.staysafe.viewModel.MapViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation() {
    val nav = rememberNavController()

    // Debugging purposes
    // * ✅ Add Logging Interceptor
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // * ✅ Create OkHttpClient with Logging
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://softwarehub.uk/unibase/staysafe/v1/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val service = retrofit.create(Service::class.java)

    val sharedViewModel: MapViewModel = remember {
        MapViewModel(
            repository = StaySafeRepository(
                service = service
            )
        )
    }

    NavHost(
        navController = nav,
        startDestination = Screen.LoginScreen.route
    ) {
        // //
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        // //
        composable(Screen.MapScreen.route) {
            MapScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        // //
        composable(Screen.RegisterUserScreen.route) {
            RegisterUserScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        // //
        composable(Screen.AddUserScreen.route) {
            AddUserScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
    }
}
