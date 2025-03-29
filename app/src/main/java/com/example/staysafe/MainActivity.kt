package com.example.staysafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.staysafe.nav.Navigation
import com.example.staysafe.service.NotificationService
import com.example.staysafe.ui.theme.StaySafeTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            when (entry.key) {
                Manifest.permission.CAMERA -> {
                    if (entry.value) {
                        Log.d("MainActivity", "✅ Camera permission granted")
                    } else {
                        Log.e("MainActivity", "❌ Camera permission denied")
                    }
                }
                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (entry.value) {
                        Log.d("MainActivity", "✅ Notification permission granted")
                    } else {
                        Log.e("MainActivity", "❌ Notification permission denied")
                    }
                }
                Manifest.permission.ACTIVITY_RECOGNITION ->{
                    if (entry.value) {
                        Log.d("MainActivity", "✅ Activity permission granted")
                    }else{
                        Log.d("MainActivity", "❌ Activity permission denied")
                    }
                }
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Log.d("MainActivity", "✅ Photo captured successfully")
        } else {
            Log.e("MainActivity", "❌ Failed to capture photo")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        val permissions = mutableListOf<String>()
        
        // Camera permission
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        // Notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Activity permission for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // Request permissions if any are needed
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }

        // Create notification channel
        NotificationService.createNotificationChannel(this)

        setContent {
            StaySafeTheme {
                Navigation()
            }
        }
    }

    fun launchCamera(photoUri: android.net.Uri) {
        takePictureLauncher.launch(photoUri)
    }
}
