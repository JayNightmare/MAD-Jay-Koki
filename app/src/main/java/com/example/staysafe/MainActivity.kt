package com.example.staysafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.staysafe.Navigator.ScreenNavigator
import com.example.staysafe.ViewModel.SafeViewModel
import com.example.staysafe.ui.theme.StaySafeTheme
import com.example.staysafe.utils.LocationPermissionHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val viewModel: SafeViewModel = viewModel()
            var permissionGranted by remember { mutableStateOf(false) }
            
            StaySafeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationPermissionHandler(
                        onPermissionGranted = {
                            permissionGranted = true
                        }
                    ) {
                        val navController = rememberNavController()
                        ScreenNavigator(navController)
                    }
                }
            }
        }
    }
}
