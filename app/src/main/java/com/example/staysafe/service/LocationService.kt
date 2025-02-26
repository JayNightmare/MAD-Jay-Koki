package com.example.staysafe.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getLocationUpdates(intervalMs: Long = 5000): Flow<LatLng> = callbackFlow {
        if (!hasLocationPermission()) {
            throw LocationException("Missing location permission")
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(10f)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    launch {
                        send(LatLng(location.latitude, location.longitude))
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        ).addOnFailureListener { e ->
            close(e)
        }

        // Remove location updates when Flow collection stops
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    suspend fun getCurrentLocation(): LatLng? {
        if (!hasLocationPermission()) {
            throw LocationException("Missing location permission")
        }

        return try {
            fusedLocationClient.lastLocation.await()?.let { location ->
                LatLng(location.latitude, location.longitude)
            }
        } catch (e: Exception) {
            null
        }
    }
}

class LocationException(message: String) : Exception(message)
