package com.example.staysafe.nav

import RegisterUserScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var startDestination by remember { mutableStateOf(Screen.LoginScreen.route) }

    // Check for emergency screen intent
    LaunchedEffect(Unit) {
        val intent = (context as? android.app.Activity)?.intent
        intent?.getStringExtra("screen")?.let { screen ->
            if (screen == "emergency") {
                startDestination = "emergency/${intent.getLongExtra("userId", 0)}/${intent.getLongExtra("panicTime", 0)}"
            }
        }
    }

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
            ),
            context = context
        )
    }

    NavHost(
        navController = nav,
        startDestination = startDestination
    ) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        composable(Screen.MapScreen.route) {
            MapScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        composable(Screen.RegisterUserScreen.route) {
            RegisterUserScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        composable(Screen.AddScreen.route) {
            AddScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        composable(Screen.ProfileScreen.route) {
            ProfileScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        composable(Screen.SettingsScreen.route) {
            SettingsScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        composable(Screen.UserActivitiesScreen.route) {
            UserActivitiesScreen(
                navController = nav,
                viewModel = sharedViewModel
            )
        }
        composable("emergency/{userId}/{panicTime}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull()
            val panicTime = backStackEntry.arguments?.getString("panicTime")?.toLongOrNull()
            
            if (userId != null && panicTime != null) {
                EmergencyScreen(
                    user = sharedViewModel.getUserById(userId),
                    activity = sharedViewModel.getCurrentActivity(),
                    panicTime = panicTime,
                    onClose = { nav.navigate(Screen.MapScreen.route) },
                    onGetDirections = {
                        sharedViewModel.getUserById(userId)?.let { user ->
                            user.userLatitude?.let { lat ->
                                user.userLongitude?.let { lon ->
                                    sharedViewModel.openDirections(lat, lon)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
