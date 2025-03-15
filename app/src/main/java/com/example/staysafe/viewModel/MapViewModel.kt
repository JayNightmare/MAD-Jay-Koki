package com.example.staysafe.viewModel

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.staysafe.model.data.*
import com.example.staysafe.repository.StaySafeRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MapViewModel(
    private val repository: StaySafeRepository
) : ViewModel() {

    // Users
    private val _users = MutableStateFlow<List<User>>(emptyList())
    @SuppressLint("RestrictedApi")
    val users: StateFlow<List<User>> = _users

    init {
        fetchAllData()
    }

    private fun fetchAllUsers() {
        println("DEBUG: fetchAllUsers() CALLED")  // ✅ Log when function is called

        viewModelScope.launch {
            println("DEBUG: fetchAllUsers() - Inside viewModelScope.launch")  // ✅ Log inside coroutine

            repository.getAllUsers()
                .collect { users ->
                    println("DEBUG: fetchAllUsers() - Received ${users.size} users")  // ✅ Log received data
                    _users.value = users
                }
        }
    }



    fun fetchUserById(id: Long) {
        viewModelScope.launch {
            repository.getUserById(id).collect { _users.value = it }
        }
    }

    // Activities
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private fun fetchAllActivities() {
        viewModelScope.launch {
            repository.getAllActivities().collect { _activities.value = it }
        }
    }

    fun fetchActivityById(id: Long) {
        viewModelScope.launch {
            repository.getActivityById(id).collect { _activities.value = it }
        }
    }

    // Locations
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    private fun fetchAllLocations() {
        viewModelScope.launch {
            repository.getAllLocations().collect { _locations.value = it }
        }
    }

    @OptIn(UnstableApi::class)
    fun fetchLocationById(userId: Long): Flow<Location?> {
        return repository.getLocationById(userId)
            .map { locations ->
                if (locations.isNotEmpty()) {
                    Log.d("MapViewModel", "✅ Location found for userId $userId: ${locations.first()}")
                    locations.first() // Return the first location
                } else {
                    Log.e("MapViewModel", "❌ No location found for userId: $userId")
                    null // Emit null if no location exists
                }
            }
            .catch { e ->
                Log.e("MapViewModel", "❌ Error fetching location: ${e.message}")
                emit(null) // Emit null in case of an error
            }
            .flowOn(Dispatchers.IO)
    }

    // Positions
    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    val positions: StateFlow<List<Position>> = _positions

    private fun fetchAllPositions() {
        viewModelScope.launch {
            repository.getAllPositions().collect { _positions.value = it }
        }
    }

    fun fetchPositionById(id: Long) {
        viewModelScope.launch {
            repository.getPositionById(id).collect { _positions.value = it }
        }
    }

    // Fetch all data at once
    fun fetchAllData() {
        fetchAllUsers()
        fetchAllActivities()
        fetchAllLocations()
        fetchAllPositions()
    }

    // Fetch Route
    @OptIn(UnstableApi::class)
    fun fetchRoute(
        start: LatLng,
        end: LatLng,
        apiKey: String,
        onResult: (List<LatLng>) -> Unit
    ) {
        viewModelScope.launch {
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&key=$apiKey"

            try {
                val response = withContext(Dispatchers.IO) {
                    URL(url).readText()
                }
                val jsonObject = JSONObject(response)
                val routes = jsonObject.getJSONArray("routes")

                if (routes.length() > 0) {
                    val points = decodePolyline(
                        routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points")
                    )
                    onResult(points)
                } else {
                    Log.e("FetchRoute", "No routes found")
                }
            } catch (e: Exception) {
                Log.e("FetchRoute", "Error fetching route", e)
            }
        }
    }


    @OptIn(UnstableApi::class)
    suspend fun fetchDistanceAndDuration(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        apiKey: String
    ): Pair<String, String>? {
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=$originLat,$originLng&destination=$destLat,$destLng&key=$apiKey"

        return try {
            val response = withContext(Dispatchers.IO) {
                URL(url).readText()
            }
            val jsonObject = JSONObject(response)
            val routes = jsonObject.getJSONArray("routes")

            if (routes.length() > 0) {
                val legs = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                val distance = legs.getJSONObject("distance").getString("text") // e.g., "5.3 km"
                val duration = legs.getJSONObject("duration").getString("text") // e.g., "12 mins"
                Log.d("FetchRoute", "Distance: $distance, Duration: $duration")
                Pair(distance, duration)
            } else {
                Log.e("FetchRoute", "No routes found")
                null
            }
        } catch (e: Exception) {
            Log.e("FetchRoute", "Error fetching distance and duration", e)
            null
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val polyline = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)

            val deltaLat = (if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1))
            lat += deltaLat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)

            val deltaLng = (if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1))
            lng += deltaLng

            polyline.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return polyline
    }
}
