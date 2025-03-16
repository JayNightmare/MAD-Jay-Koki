package com.example.staysafe.viewModel

import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
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
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.Instant

class MapViewModel(
    private val repository: StaySafeRepository
) : ViewModel() {
    // ! Contacts (Only show users in the Contact Table)
    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts: StateFlow<List<User>> = _contacts

    // ! Status of user (planned, active, completed)
    private val _userStatus = MutableStateFlow<Status?>(null)
    val userStatus: StateFlow<Status?> = _userStatus

    // ! Activities of user
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    // ! Locations of user
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    // ! Positions for live tracking
    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    val positions: StateFlow<List<Position>> = _positions

    // ! User
    private val _user = MutableStateFlow<List<User>>(emptyList())
    val user: StateFlow<List<User>> = _user

    // ! Individual user details
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    // ! Logged in user
    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser

    fun setLoggedInUser(user: User) {
        _loggedInUser.value = user
    }

    // //
    // * Authenticate User
    fun authenticateUser(user: User, password: String): Boolean {
        return user.userPassword == password // Simple password check (For demo purposes)
    }

    // //

    // //
    // * Users
    fun fetchAllUsers() {
        println("DEBUG: fetchAllUsers() CALLED")  // ✅ Log when function is called

        viewModelScope.launch {
            println("DEBUG: fetchAllUsers() - Inside viewModelScope.launch")  // ✅ Log inside coroutine

            repository.getAllUsers()
                .collect { user ->
                    println("DEBUG: fetchAllUsers() - Received ${user.size} contacts")  // ✅ Log received data
                    _user.value = user
                }
        }
    }

    fun fetchUserById(id: Long) {
        viewModelScope.launch {
            repository.getUserById(id).collect { users ->
                _selectedUser.value = users.firstOrNull() // Expecting a single user
            }
        }
    }
    // //

    // //
    // * Activities
    private fun fetchAllActivities() {
        viewModelScope.launch {
            repository.getAllActivities().collect { _activities.value = it }
        }
    }

    fun fetchActivityByUser(userId: Long) {
        viewModelScope.launch {
            repository.getActivityByUser(userId).collect { _activities.value = it }
        }
    }
    // //

    // //
    // * Locations
    private fun fetchAllLocations() {
        viewModelScope.launch {
            repository.getAllLocations().collect { _locations.value = it }
        }
    }

    @OptIn(UnstableApi::class)
    fun fetchLocationById(userId: Long): Flow<Location?> {
        return repository.getLocationById(userId)
            .map { locations -> locations.firstOrNull() }
            .catch { e ->
                Log.e("MapViewModel", "Error fetching location: ${e.message}")
                emit(null)
            }
            .flowOn(Dispatchers.IO)
    }
    // //

    // //
    // * Positions
    private fun fetchAllPositions() {
        viewModelScope.launch {
            repository.getAllPositions().collect { _positions.value = it }
        }
    }

    fun fetchPositionByUser(userId: Long) {
        viewModelScope.launch {
            repository.getPositionById(userId).collect { _positions.value = it }
        }
    }
    // //

    // //
    // * Contacts
    fun fetchUserContacts(userId: Long) {
        viewModelScope.launch { repository.getContactsForUser(userId).collect { _contacts.value = it } }
    }
    // //

    // //
    // * Status
    @OptIn(UnstableApi::class)
    suspend fun fetchStatusByUserId(userId: Long): Flow<Status?> {
        return repository.getStatus()
            .map { statuses ->
                statuses.find { it.statusID == userId } // Find status for the user
            }
            .catch { e ->
                Log.e("MapViewModel", "Error fetching status: ${e.message}")
                emit(null)
            }
            .flowOn(Dispatchers.IO)
    }
    // //

    // //
    // ? Fetch all data at once
//    private fun fetchAllData() {
//        fetchAllUsers()
//        fetchAllActivities()
//        fetchAllLocations()
//        fetchAllPositions()
//    }
    // //

    // //
    // ! Function

    // * Fetch Route
    @OptIn(UnstableApi::class)
    fun fetchRoute(start: LatLng, end: LatLng, apiKey: String, onResult: (List<LatLng>) -> Unit) {
        viewModelScope.launch {
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&key=$apiKey"
            try {
                val response = withContext(Dispatchers.IO) { URL(url).readText() }
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

    // * Fetch Distance and Duration
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    suspend fun fetchDistanceAndDuration(
        user: User,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        apiKey: String
    ): Pair<String, String>? {
        val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes")

        Log.d("RoutesAPI", "Calling URL: $url")

        val friendLon = user.userLongitude
        val friendLat = user.userLatitude

        Log.d("RoutesAPI", "Origin: $friendLat, $friendLon")
        Log.d("RoutesAPI", "Destination: $destLat, $destLng")

        val departureTime = Instant.now().plus(Duration.ofMinutes(1)).toString()

        val body = """
    {
        "origin": {
            "location": {
                "latLng": {
                    "latitude": $friendLat,
                    "longitude": $friendLon
                }
            }
        },
        "destination": {
            "location": {
                "latLng": {
                    "latitude": $destLat,
                    "longitude": $destLng
                }
            }
        },
        "travelMode": "DRIVE",
        "routingPreference": "TRAFFIC_AWARE",
        "departureTime": "$departureTime",
        "computeAlternativeRoutes": false,
        "routeModifiers": {
            "avoidTolls": false,
            "avoidHighways": false,
            "avoidFerries": false
        },
        "languageCode": "en-US",
        "units": "METRIC"
    }
    """.trimIndent()

        Log.d("RoutesAPI", "Request Body: $body")

        try {
            return withContext(Dispatchers.IO) {
                (url.openConnection() as? HttpURLConnection)?.let { connection ->
                    connection.doOutput = true
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connection.setRequestProperty("X-Goog-Api-Key", apiKey)
                    connection.setRequestProperty(
                        "X-Goog-FieldMask",
                        "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"
                    )

                    connection.outputStream.use { os ->
                        os.write(body.toByteArray(Charsets.UTF_8))
                    }

                    val responseCode = connection.responseCode
                    Log.d("RoutesAPI", "Response Code: $responseCode")
                    val responseMessage = connection.responseMessage
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("RoutesAPI", "Raw Response: $response")

                    Log.d("RoutesAPI", "Response Code: $responseCode")
                    Log.d("RoutesAPI", "Raw Response: $response")

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        Log.e("RoutesAPI", "❌ API Error - HTTP $responseCode: $responseMessage")
                        Log.e("RoutesAPI", "❌ Error Response: $response")
                        return@let null
                    }

                    val json = JSONObject(response)
                    if (!json.has("routes") || json.getJSONArray("routes").length() == 0) {
                        Log.e("RoutesAPI", "❌ No routes found!")
                        return@let null
                    }

                    val route = json.getJSONArray("routes").getJSONObject(0)
                    val distanceMeters = route.optDouble("distanceMeters", 0.0)

                    val durationString = route.optString("duration", "0s")
                    Log.d("RoutesAPI", "Duration String: $durationString")

                    val durationSeconds = parseDuration(durationString)
                    val formattedDuration = formatDuration(durationSeconds)

                    Log.d("RoutesAPI", "✅ Distance: ${distanceMeters / 1000} km")
                    Log.d("RoutesAPI", "✅ Duration: $formattedDuration")

                    val distanceKm = String.format("%.2f km", distanceMeters / 1000)

                    return@let distanceKm to formattedDuration
                }
            }
        } catch (e: Exception) {
            Log.e("RoutesAPI", "❌ Error: ${e.message}")
            return null
        }
    }

    // * Parse Duration
    private fun parseDuration(duration: String): Int {
        val regex = Regex("(\\d+)s") // ✅ Extracts only the number before "s"
        val match = regex.find(duration)
        return match?.groupValues?.get(1)?.toInt() ?: 0
    }

    // * Format Duration
    private fun formatDuration(seconds: Int): String {
        return when {
            seconds < 60 -> "$seconds sec"
            seconds < 3600 -> "${seconds / 60} min"
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                if (minutes == 0) "${hours}h" else "${hours}h ${minutes}m"
            }
        }
    }

    // * Decode Polyline
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

            val deltaLat = if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1)
            lat += deltaLat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)

            val deltaLng = if ((result and 1) != 0) (result.inv() shr 1) else (result shr 1)
            lng += deltaLng

            polyline.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return polyline
    }
    // //
}
