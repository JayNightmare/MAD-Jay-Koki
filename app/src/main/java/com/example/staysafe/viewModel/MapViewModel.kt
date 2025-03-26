package com.example.staysafe.viewModel

import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.staysafe.BuildConfig
import com.example.staysafe.model.data.*
import com.example.staysafe.repository.StaySafeRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
class MapViewModel
    (
    val repository: StaySafeRepository
) : ViewModel() {
    init {
        Log.d("Flow", "✅ MapViewModel Initialized")
        fetchAllLocations() // Fetch all locations when ViewModel is created
    }

    // ! Contacts (Only show users in the Contact Table)
    private val _contacts = MutableStateFlow<List<UserWithContact>>(emptyList())
    val contacts: StateFlow<List<UserWithContact>> = _contacts

    private val _contact = MutableStateFlow<List<Contact>>(emptyList())
    val contact: StateFlow<List<Contact>> = _contact

    // ! Status of user (planned, active, completed)
    private val _userStatus = MutableStateFlow<Status?>(null)
    val userStatus: StateFlow<Status?> = _userStatus

    // ! Activities of user
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    // ! Latest Activity for user
    private val _latestActivityForUser = MutableStateFlow<Activity?>(null)
    val latestActivityForUser: StateFlow<Activity?> = _latestActivityForUser

    // ! Latest Activities for users
    private val _latestActivities = MutableStateFlow<Map<Long, Activity?>>(emptyMap())
    val latestActivities: StateFlow<Map<Long, Activity?>> = _latestActivities

    // ! Locations of user
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    // ! Positions for Map Routing
    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    val positions: StateFlow<List<Position>> = _positions

    // ! Latest Position for user
    private val _latestPosition = MutableStateFlow<Position?>(null)
    val latestPosition: StateFlow<Position?> = _latestPosition

    // ! User
    private val _user = MutableStateFlow<List<User>>(emptyList())
    val user: StateFlow<List<User>> = _user

    // ! Individual user details
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    // ! Logged in user
    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser

    @OptIn(UnstableApi::class)
    fun setLoggedInUser(username: String) {
        Log.d("Flow", "Setting logged in user to: $username")
        viewModelScope.launch {
            val matchedUser = _user.value.find { it.userUsername == username }
            Log.d("Flow", "Matched User: $username")
            if (matchedUser != null) {
                _loggedInUser.update { matchedUser }
                Log.d("Flow", "✅ Logged in as: ${matchedUser.userUsername}")
            } else {
                Log.e("Flow", "❌ User not found for username: $username")
            }
        }
    }
    // //

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

    fun createUser(
        firstName: String,
        lastName: String,
        phone: String,
        username: String,
        password: String,
        userLatitude: Double?,
        userLongitude: Double?
    ): User? {
        val existingUser = _user.value.find { it.userUsername == username }
        if (existingUser != null) return null

        val newUser = User(
            userID = (_user.value.maxOfOrNull { it.userID } ?: 0) + 1L,
            userFirstname = firstName,
            userLastname = lastName,
            userPhone = phone,
            userUsername = username,
            userPassword = password,
            userLatitude = userLatitude,
            userLongitude = userLongitude,
            userTimestamp = System.currentTimeMillis(),
            userImageURL = "https://avatar.iran.liara.run/public"
        )

        _user.value += newUser

        viewModelScope.launch {
            repository.addUser(newUser).collect()
        }

        return newUser
    }

    fun deleteUserByID (userId: Long):User?{
        val existingUser = _user.value.find { it.userID == userId } ?: return null
        _user.value -= existingUser

        viewModelScope.launch {
            repository.deleteUser(userId).collect(){
                logout()
            }
        }
        return existingUser
    }

    fun logout() {
        _loggedInUser.value = null
    }
    // //

    private val _updateResult = MutableStateFlow<Boolean?>(null)
    val updateResult: StateFlow<Boolean?> = _updateResult

    private var locationUpdateJob: Job? = null
    private val locationUpdateInterval = 5 * 60 * 1000L // 5 minutes in milliseconds

    fun startLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = viewModelScope.launch {
            while (isActive) {
                updateUserLocation()
                delay(locationUpdateInterval)
            }
        }
    }

    fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }

    private suspend fun updateUserLocation() {
        try {
            val loggedInUser = _loggedInUser.value
            if (loggedInUser == null) {
                Log.e("updateUserLocation", "❌ No logged in user found")
                return
            }

            // Get current location from the latest position
            val latestPosition = _latestPosition.value
            if (latestPosition == null) {
                Log.e("updateUserLocation", "❌ No location data available")
                return
            }

            // Update user with new location
            val updatedUser = loggedInUser.copy(
                userLatitude = latestPosition.positionLatitude,
                userLongitude = latestPosition.positionLongitude
            )

            repository.updateUser(updatedUser).collect { result ->
                if (result is List<*> && result.isNotEmpty()) {
                    Log.d("updateUserLocation", "✅ Location updated successfully")
                } else {
                    Log.e("updateUserLocation", "❌ Failed to update location")
                }
            }
        } catch (e: Exception) {
            Log.e("updateUserLocation", "❌ Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            repository.updateUser(user).collect { result ->
                _updateResult.value = result is List<*> && result.isNotEmpty()
            }
        }
    }

    // //
    // * Activities
    private fun fetchAllActivities() {
        viewModelScope.launch {
            repository.getAllActivities().collect { _activities.value = it }
        }
    }

    fun fetchLatestActivityForUser(userId: Long) {
        viewModelScope.launch {
            repository.getLatestActivityForUser(userId).collect { activity ->
                _latestActivityForUser.value = activity
                Log.d("MapViewModel", "Latest Activity for user $userId: $activity")
            }
        }
    }

    fun fetchLatestActivityForUsers(userId: Long) {
        viewModelScope.launch {
            repository.getLatestActivityForUser(userId).collect { activity ->
                _latestActivities.update { currentActivities ->
                    currentActivities + (userId to activity) // ✅ Store latest activity per user
                }
            }
        }
    }

    fun fetchActivitiesForUser(userId: Long) {
        viewModelScope.launch {
            repository.getActivitiesForUser(userId).collect { activitiesList ->
                _activities.value = activitiesList
                Log.d("fetchActivitiesForUser", "✅ Fetched ${activitiesList.size} activities for user $userId")
            }
        }
    }

    fun addActivity(
        name: String,
        fromActivityName: String?,
        toActivityName: String?,
        startAddressLine: String,
        destAddressLine: String,
        description: String,
        fromISOTime: String,
        toisoTime: String,
        fromPostcode: String = "",
        toPostcode: String = ""
    ) {
        viewModelScope.launch {
            try {
                val loggedInUser = _loggedInUser.value
                if (loggedInUser == null) {
                    Log.e("addActivity", "❌ No logged-in user!")
                    return@launch
                }

                Log.d("addActivity", "✅ Adding new activity for user ${loggedInUser.userUsername}...")

                // Get the max activity ID from the local state
                val newActivityID = (_activities.value.maxOfOrNull { it.activityID } ?: 0) + 1L
                
                // Fetch all locations first to ensure we have the latest data
                fetchAllLocations()
                
                // Get the max location ID from the API (wait for completion)
                var maxLocationId = 0
                repository.getMaxLocationId().collect { maxId ->
                    maxLocationId = maxId
                    Log.d("addActivity", "Max location ID from API: $maxLocationId")
                }
                
                // Generate unique Location IDs
                val fromLocationID = maxLocationId + 1
                val toLocationID = maxLocationId + 2
                
                Log.d("addActivity", "Generated Activity ID: $newActivityID")
                Log.d("addActivity", "Generated From Location ID: $fromLocationID")
                Log.d("addActivity", "Generated To Location ID: $toLocationID")

                // Get coordinates for addresses using geocoding
                val fromAddressWithPostcode = if (fromPostcode.isNotBlank()) "$startAddressLine, $fromPostcode" else startAddressLine
                val toAddressWithPostcode = if (toPostcode.isNotBlank()) "$destAddressLine, $toPostcode" else destAddressLine
                
                val fromCoordinates = geocodeAddress(fromAddressWithPostcode)
                val toCoordinates = geocodeAddress(toAddressWithPostcode)
                
                if (fromCoordinates == null || toCoordinates == null) {
                    Log.e("addActivity", "❌ Could not geocode addresses - aborting activity creation")
                    return@launch
                }
                
                Log.d("addActivity", "From coordinates: $fromCoordinates")
                Log.d("addActivity", "To coordinates: $toCoordinates")
                
                // Create location objects
                val fromLocation = Location(
                    locationID = fromLocationID,
                    locationName = fromActivityName,
                    locationDescription = description,
                    locationAddress = startAddressLine,
                    locationPostcode = fromPostcode,
                    locationLatitude = fromCoordinates.first,
                    locationLongitude = fromCoordinates.second
                )
                
                val toLocation = Location(
                    locationID = toLocationID,
                    locationName = toActivityName,
                    locationDescription = description,
                    locationAddress = destAddressLine, 
                    locationPostcode = toPostcode,
                    locationLatitude = toCoordinates.first,
                    locationLongitude = toCoordinates.second
                )
                
                Log.d("addActivity", "Created From Location: $fromLocation")
                Log.d("addActivity", "Created To Location: $toLocation")
                
                // Add the From location first, wait for completion
                var fromLocationSuccess = false
                repository.addLocation(fromLocation).collect { response ->
                    if (response != null) {
                        Log.d("addActivity", "✅ From location added successfully!")
                        _locations.value += fromLocation
                        fromLocationSuccess = true
                    } else {
                        Log.e("addActivity", "❌ Failed to add from location!")
                    }
                }
                
                if (!fromLocationSuccess) {
                    Log.e("addActivity", "❌ From location creation failed, aborting activity creation")
                    return@launch
                }
                
                // Add the To location next, wait for completion
                var toLocationSuccess = false
                repository.addLocation(toLocation).collect { response ->
                    if (response != null) {
                        Log.d("addActivity", "✅ To location added successfully!")
                        _locations.value += toLocation
                        toLocationSuccess = true
                    } else {
                        Log.e("addActivity", "❌ Failed to add to location!")
                    }
                }
                
                if (!toLocationSuccess) {
                    Log.e("addActivity", "❌ To location creation failed, aborting activity creation")
                    return@launch
                }
                
                // Both locations were added successfully, now create the activity
                val newActivity = Activity(
                    activityID = newActivityID,
                    activityName = name,
                    activityUserID = loggedInUser.userID,
                    activityDescription = description,
                    activityFromID = fromLocationID.toLong(),
                    activityLeave = fromISOTime,
                    activityToID = toLocationID.toLong(),
                    activityArrive = toisoTime,
                    activityStatusID = 1L,
                    activityUsername = loggedInUser.userUsername,
                    activityFromName = startAddressLine,
                    activityToName = destAddressLine,
                    activityStatusName = "Planned"
                )
                
                // Finally, add the activity
                repository.addActivity(newActivity).collect { response ->
                    if (response != null) {
                        Log.d("addActivity", "✅ Activity added successfully!")
                        Log.d("addActivity", "✅ Response: $response")
                        _activities.value += response
                    } else {
                        Log.e("addActivity", "❌ Failed to add activity!")
                    }
                }
            } catch (e: Exception) {
                Log.e("addActivity", "❌ Exception occurred: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun deleteActivity (activityId: Long):Activity?{
        val existingActivities = _activities.value.find { it.activityID == activityId } ?: return null
        _activities.value -= existingActivities

        viewModelScope.launch {
            repository.deleteActivity(activityId).collect()
        }
        return existingActivities
    }
    
    private suspend fun geocodeAddress(address: String): Pair<Double, Double>? {
        val apiKey = BuildConfig.MAP_API_GOOGLE
        val encodedAddress = address.replace(" ", "+")
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$apiKey"
        
        try {
            val response = withContext(Dispatchers.IO) { URL(url).readText() }
            val jsonObject = JSONObject(response)
            
            if (jsonObject.getString("status") == "OK") {
                val results = jsonObject.getJSONArray("results")
                if (results.length() > 0) {
                    val location = results.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location")
                    
                    val lat = location.getDouble("lat")
                    val lng = location.getDouble("lng")
                    
                    return Pair(lat, lng)
                }
            }
            Log.e("geocodeAddress", "❌ Failed to geocode address: $address, Status: ${jsonObject.getString("status")}")
            return null
        } catch (e: Exception) {
            Log.e("geocodeAddress", "❌ Exception geocoding address: ${e.message}")
            return null
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
    fun fetchLocationById(locationID: Long): Flow<Location?> {
        return repository.getLocationById(locationID)
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
    // //

    // //
    // * Contacts
    @OptIn(UnstableApi::class)
    fun fetchUserContacts(userId: Long) {
        Log.d("MapViewModel", "Fetching user contacts for userId: $userId")
        viewModelScope.launch {
            repository.getContactsForUser(userId).collect { _contacts.value = it }
        }
    }

    fun addContact(username: String, phone: String, label: String = "Friend") {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                val matchingUser =
                    users.find { it.userUsername == username && it.userPhone == phone }

                if (matchingUser != null) {
                    val loggedInUserId = _loggedInUser.value?.userID ?: return@collect

                    val existingContact = _contact.value.find {
                        it.contactContactID == matchingUser.userID && it.contactUserID == loggedInUserId
                    }

                    if (existingContact != null) {
                        Log.e("addContact", "❌ User is already in your contacts!")
                        return@collect
                    }

                    Log.d("addContact", "✅ User found! Adding to contacts...")

                    val newContactID = (_contact.value.maxOfOrNull { it.contactID } ?: 0) + 1L

                    val dateFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date())

                    val newContact = Contact(
                        contactID = newContactID,
                        contactUserID = loggedInUserId,
                        contactContactID = matchingUser.userID,
                        contactLabel = label,
                        contactDateCreated = formattedDate
                    )

                    repository.addContact(newContact).collect { response ->
                        if (response != null) {
                            Log.d("addContact", "✅ Contact added successfully!")
                            _contact.value += response
                        } else {
                            Log.e("addContact", "❌ Failed to add contact!")
                        }
                    }
                } else {
                    Log.e("addContact", "❌ User not found!")
                }
            }
        }
    }

    fun removeContact(contactUserId: Long, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val loggedInUserId = _loggedInUser.value?.userID
                if (loggedInUserId == null) {
                    Log.e("removeContact", "❌ No logged in user found")
                    onComplete(false)
                    return@launch
                }

                Log.d("removeContact", "Removing contact relationship between $loggedInUserId and $contactUserId")
                
                // Get the contact relationship from the API response
                repository.getContactsForUser(loggedInUserId).collect { users ->
                    val contactUser = users.find { it.userID == contactUserId }
                    if (contactUser == null) {
                        Log.e("removeContact", "❌ Contact user not found")
                        onComplete(false)
                        return@collect
                    }

                    // Delete the contact using repository
                    repository.deleteContact(contactUser.userContactID).collect { success ->
                        if (success) {
                            Log.d("removeContact", "✅ Contact removed successfully")
                            // Refresh contacts list
                            fetchUserContacts(loggedInUserId)
                            onComplete(true)
                        } else {
                            Log.e("removeContact", "❌ Failed to remove contact")
                            onComplete(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("removeContact", "❌ Exception: ${e.message}")
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun sendCallNotification(userId: Long) {
        // TODO: Implement notification sending
        Log.d("MapViewModel", "Sending call notification to user: $userId")
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
            val url =
                "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&key=$apiKey"
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
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        apiKey: String
    ): Pair<String, String>? {
        val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes")

        Log.d("RoutesAPI", "Calling URL: $url")

        val friendLon = originLat
        val friendLat = originLng

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

    fun getRouteForLocation(
        startPostCode: String,
        startAddressLine: String,
        destPostCode: String,
        destAddressLine: String,
        travelMode: String,
        onResult: (List<LatLng>) -> Unit
    ) {
        viewModelScope.launch {
            getRoute(startPostCode, startAddressLine, destPostCode, destAddressLine).collect { routePoints ->
                if (routePoints.isNotEmpty()) {
                    onResult(routePoints)
                } else {
                    Log.e("getRouteForLocation", "❌ No route found for $startAddressLine to $destAddressLine")
                }
            }
        }
    }

    // * Other Functions
    private suspend fun getRoute(
        startPostCode: String,
        startAddressLine: String,
        postCode: String,
        address: String,
        apiKey: String = BuildConfig.MAP_API_GOOGLE
    ): Flow<List<LatLng>> = flow {

        // ! Format the destination query string
        val destination = "$postCode, $address".replace(" ", "+") // URL Encode Spaces
        val startLocation = "$startPostCode, $startAddressLine".replace(" ", "+")

        // ! Construct Google Maps Directions API URL
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=$startLocation" +
                "&destination=$destination" +
                "&key=$apiKey"

        try {
            val response = withContext(Dispatchers.IO) { URL(url).readText() }
            val jsonObject = JSONObject(response)

            val routesArray = jsonObject.getJSONArray("routes")
            if (routesArray.length() > 0) {
                val overviewPolyline =
                    routesArray.getJSONObject(0).getJSONObject("overview_polyline")
                val encodedPolyline = overviewPolyline.getString("points")

                val decodedPoints = decodePolyline(encodedPolyline)
                emit(decodedPoints) // ✅ Return the list of LatLng points
            } else {
                android.util.Log.e("getRoute", "❌ No route found for destination: $destination")
                emit(emptyList()) // Return empty list if no route is found
            }
        } catch (e: Exception) {
            android.util.Log.e("getRoute", "❌ Error fetching route: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    fun changePassword(currentPassword: String, newPassword: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val loggedInUser = _loggedInUser.value
                if (loggedInUser == null) {
                    Log.e("changePassword", "❌ No logged in user found")
                    onComplete(false)
                    return@launch
                }

                Log.d("changePassword", "Current user: ${loggedInUser.userUsername}")
                Log.d("changePassword", "Current password provided: $currentPassword")
                Log.d("changePassword", "Current password in DB: ${loggedInUser.userPassword}")

                // ! Verify current password
                if (loggedInUser.userPassword != currentPassword) {
                    Log.e("changePassword", "❌ Current password verification failed")
                    onComplete(false)
                    return@launch
                }

                // ! Create updated user object
                val updatedUser = loggedInUser.copy(
                    userPassword = newPassword
                )

                Log.d("changePassword", "Updating user with new password")
                Log.d("changePassword", "User ID: ${updatedUser.userID}")
                Log.d("changePassword", "New password: $newPassword")

                // ! Update user in repository
                repository.updateUser(updatedUser).collect { result ->
                    Log.d("changePassword", "Update result: $result")
                    if (result is List<*>) {
                        val updatedUserList = result.filterIsInstance<User>()
                        if (updatedUserList.isNotEmpty()) {
                            _loggedInUser.update { updatedUserList.first() }
                            Log.d("changePassword", "✅ Password updated successfully")
                            onComplete(true)
                        } else {
                            Log.e("changePassword", "❌ No user found in response list")
                            onComplete(false)
                        }
                    } else {
                        Log.e("changePassword", "❌ Failed to update password - invalid result type")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("changePassword", "❌ Exception: ${e.message}")
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
    // //

}
