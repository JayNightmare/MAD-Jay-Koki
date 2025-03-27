package com.example.staysafe.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.content.Context
import android.content.IntentFilter
import android.os.BatteryManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.app.NotificationCompat
import com.example.staysafe.R
import com.example.staysafe.model.data.User
import com.example.staysafe.model.data.UserWithContact
import com.example.staysafe.repository.StaySafeRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SafetyMonitoringService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var repository: StaySafeRepository
    private var lastKnownLocation: LatLng? = null
    private var currentActivity: String? = null
    private var checkInJob: Job? = null
    private var currentUser: UserWithContact? = null
    
    companion object {
        private const val TAG = "SafetyMonitoringService"
        private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
        private const val LOCATION_FASTEST_INTERVAL = 5000L // 5 seconds
        private const val MAX_ROUTE_DEVIATION = 100.0 // meters
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

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

        val service =  retrofit.create(com.example.staysafe.API.Service::class.java)
        repository = StaySafeRepository(
            service = service
        )
        setupLocationCallback()
        startLocationUpdates()
        startPeriodicCheckIns()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    checkRouteDeviation(newLocation)
                    updateUserLocation(newLocation)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
        }
    }

    private fun checkRouteDeviation(newLocation: LatLng) {
        // TODO: Implement route deviation detection
        // Compare newLocation with planned route
        // If deviation exceeds MAX_ROUTE_DEVIATION, trigger alert
    }

    private fun updateUserLocation(location: LatLng) {
        currentUser?.let { user ->
            val updatedUser = user.copy(
                userLatitude = location.latitude,
                userLongitude = location.longitude,
                userTimestamp = System.currentTimeMillis()
            )
            // Update user location in repository
            CoroutineScope(Dispatchers.IO).launch {
                repository.updateUser(updatedUser)
            }
        }
    }

    private fun startPeriodicCheckIns() {
        checkInJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                performSafetyCheck()
                kotlinx.coroutines.delay(300000) // Check every 5 minutes
            }
        }
    }

    private fun performSafetyCheck() {
        // Check battery level
        val batteryLevel = getBatteryLevel()
        if (batteryLevel < 15) {
            sendLowBatteryAlert()
        }

        // Check network connectivity
        if (!isNetworkAvailable()) {
            sendNetworkAlert()
        }

        // Check if user is moving unexpectedly
        checkUnexpectedMotion()
    }

    private fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }
        return batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            (level * 100 / scale.toFloat()).toInt()
        } ?: 0
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkUnexpectedMotion() {
        // TODO: Implement motion detection
        // Use accelerometer or gyroscope to detect unexpected movement
    }

    private fun sendLowBatteryAlert() {
        // TODO: Implement low battery notification
        val notification = NotificationCompat.Builder(this, "safety_alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Low Battery Alert")
            .setContentText("Your device battery is running low. Please charge your device.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        // Send notification
    }

    private fun sendNetworkAlert() {
        // TODO: Implement network alert notification
        val notification = NotificationCompat.Builder(this, "safety_alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Network Alert")
            .setContentText("You have lost internet connection. Some safety features may be limited.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        // Send notification
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        checkInJob?.cancel()
    }
} 