package com.example.staysafe.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.staysafe.API.Service
import com.example.staysafe.view.screens.MapScreen
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

    //Debugging purposes
    // ✅ Add Logging Interceptor
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // Logs request & response body
    }

    // ✅ Create OkHttpClient with Logging
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)  // Timeout settings
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)  // Attach logging interceptor
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://softwarehub.uk/unibase/staysafe/v1/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
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