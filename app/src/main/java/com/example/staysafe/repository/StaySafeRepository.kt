package com.example.staysafe.repository

import com.example.staysafe.ViewModel.Activity
import com.example.staysafe.ViewModel.Contact
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StaySafeRepository {
    // TODO: Replace with actual API implementation
    suspend fun createActivity(activity: Activity): Result<Activity> {
        // Simulate API call
        delay(1000)
        return Result.success(activity.copy(id = "generated_id"))
    }

    suspend fun updateActivityStatus(activityId: String, status: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }

    suspend fun updateLocation(activityId: String, location: LatLng): Result<Unit> {
        delay(200)
        return Result.success(Unit)
    }

    fun getContacts(): Flow<List<Contact>> = flow {
        // Simulate API call
        delay(1000)
        emit(listOf(
            Contact("1", "Emergency Contact", "911"),
            Contact("2", "John Doe", "+1234567890")
        ))
    }

    suspend fun addContact(contact: Contact): Result<Contact> {
        delay(500)
        return Result.success(contact.copy(id = "generated_id"))
    }

    suspend fun checkRouteDeviation(
        currentLocation: LatLng,
        startLocation: LatLng,
        endLocation: LatLng
    ): Boolean {
        // TODO: Implement actual route deviation logic using Google Maps Distance Matrix API
        // For now, using a simple straight-line distance check
        val tolerance = 500.0 // meters
        
        // Calculate if the current point is too far from the straight line between start and end
        val crossTrack = calculateCrossTrackDistance(
            currentLocation,
            startLocation,
            endLocation
        )
        
        return crossTrack > tolerance
    }

    private fun calculateCrossTrackDistance(
        point: LatLng,
        lineStart: LatLng,
        lineEnd: LatLng
    ): Double {
        // Simple cross-track distance calculation
        // This is a basic implementation and should be replaced with proper route deviation logic
        val R = 6371000.0 // Earth's radius in meters

        val δ13 = haversineDistance(lineStart, point) / R
        val θ13 = bearing(lineStart, point)
        val θ12 = bearing(lineStart, lineEnd)

        return Math.abs(Math.asin(Math.sin(δ13) * Math.sin(θ13 - θ12)) * R)
    }

    private fun haversineDistance(start: LatLng, end: LatLng): Double {
        val R = 6371000.0 // Earth's radius in meters
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    private fun bearing(start: LatLng, end: LatLng): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        
        return Math.atan2(y, x)
    }
}
