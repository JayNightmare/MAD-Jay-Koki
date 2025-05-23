package com.example.staysafe.viewModel

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.staysafe.BuildConfig
import com.example.staysafe.model.data.Activity
import com.example.staysafe.model.data.Contact
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.Position
import com.example.staysafe.model.data.Status
import com.example.staysafe.model.data.UserWithContact
import com.example.staysafe.repository.StaySafeRepository
import com.example.staysafe.service.NotificationService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
class MapViewModel (
    val repository: StaySafeRepository,
    private val context: Context
) : ViewModel() {
    private val apiKey = BuildConfig.MAP_API_GOOGLE

    private val _emergencyContacts = MutableStateFlow<List<UserWithContact>>(emptyList())
    private val emergencyContacts: StateFlow<List<UserWithContact>> = _emergencyContacts

    private val _users = MutableStateFlow<Map<Long, UserWithContact>>(emptyMap())
    val users: StateFlow<Map<Long, UserWithContact>> = _users

    init {
        loadEmergencyContacts()
        loadUsers()
        Log.d("Flow", "✅ MapViewModel Initialized")
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

    // ! Current Activity for users
    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity

    // ! Locations of user
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    // ! Positions for Map Routing
    private val _positions = MutableStateFlow<List<Position>>(emptyList())

    // ! Latest Position for user
    private val _latestPosition = MutableStateFlow<Position?>(null)
    val latestPosition: StateFlow<Position?> = _latestPosition

    // ! User
    private val _user = MutableStateFlow<List<UserWithContact>>(emptyList())
    val user: StateFlow<List<UserWithContact>> = _user

    // ! Individual user details
    private val _selectedUser = MutableStateFlow<UserWithContact?>(null)
    val selectedUser: StateFlow<UserWithContact?> = _selectedUser

    // ! Logged in user
    private val _loggedInUser = MutableStateFlow<UserWithContact?>(null)
    val loggedInUser: StateFlow<UserWithContact?> = _loggedInUser

    private val notificationService: NotificationService by lazy {
        NotificationService(context)
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserWithContact?>(null)
    val currentUser: StateFlow<UserWithContact?> = _currentUser.asStateFlow()

    private suspend fun geocodeAddress(address: String): Pair<Double, Double>? {
        val encodedAddress = address.replace(" ", "+")
        val url =
            "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$apiKey"

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
            Log.e(
                "geocodeAddress",
                "❌ Failed to geocode address: $address, Status: ${jsonObject.getString("status")}"
            )
            return null
        } catch (e: Exception) {
            Log.e("geocodeAddress", "❌ Exception geocoding address: ${e.message}")
            return null
        }
    }

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
    fun authenticateUser(user: UserWithContact, password: String): Boolean {
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
    ): UserWithContact? {
        val existingUser = _user.value.find { it.userUsername == username }
        if (existingUser != null) return null

        val newUser = UserWithContact(
            userID = (_user.value.maxOfOrNull { it.userID } ?: 0) + 1L,
            userFirstname = firstName,
            userLastname = lastName,
            userPhone = phone,
            userUsername = username,
            userPassword = password,
            userLatitude = userLatitude,
            userLongitude = userLongitude,
            userTimestamp = System.currentTimeMillis(),
            userImageURL = "https://avatar.iran.liara.run/public",
            userContactID = 0L
        )

        _user.value += newUser

        viewModelScope.launch {
            repository.addUser(newUser).collect()
        }

        return newUser
    }

    fun deleteUserByID(userId: Long): UserWithContact? {
        val existingUser = _user.value.find { it.userID == userId } ?: return null
        _user.value -= existingUser

        viewModelScope.launch {
            repository.deleteUser(userId).collect()
        }
        return existingUser
    }
    // //

    private val _updateResult = MutableStateFlow<Any?>(null)
    val updateResult: StateFlow<Any?> = _updateResult

    fun clearUpdateResult() {
        _updateResult.value = null
    }


    fun updateUserProfile(user: UserWithContact){
        viewModelScope.launch {
            repository.updateUser(user).collect { result ->
                _updateResult.value = result

                if (result != null) {
                    _loggedInUser.value = user
                }
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
                Log.d(
                    "fetchActivitiesForUser",
                    "✅ Fetched ${activitiesList.size} activities for user $userId"
                )
            }
        }
    }

    fun getRouteForActivity(activity: Activity, onRouteReceived: (List<LatLng>) -> Unit) {
        viewModelScope.launch {
            try {
                // Get locations for the activity
                val fromLocation =
                    _locations.value.find { it.locationID == activity.activityFromID.toInt() }
                val toLocation =
                    _locations.value.find { it.locationID == activity.activityToID.toInt() }

                if (fromLocation == null || toLocation == null) {
                    Log.e("getRouteForActivity", "❌ Could not find locations for activity")
                    return@launch
                }

                // Create origin and destination strings
                val origin =
                    if (fromLocation.locationLatitude != 0.0 && fromLocation.locationLongitude != 0.0) {
                        "${fromLocation.locationLatitude},${fromLocation.locationLongitude}"
                    } else {
                        "${fromLocation.locationPostcode},${fromLocation.locationAddress}"
                    }

                val destination =
                    if (toLocation.locationLatitude != 0.0 && toLocation.locationLongitude != 0.0) {
                        "${toLocation.locationLatitude},${toLocation.locationLongitude}"
                    } else {
                        "${toLocation.locationPostcode},${toLocation.locationAddress}"
                    }

                // Create URL for Google Directions API
                val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=$origin&" +
                        "destination=$destination&" +
                        "mode=driving&" +
                        "key=$apiKey"

                // Make the API request
                val response = withContext(Dispatchers.IO) { URL(url).readText() }
                val jsonObject = JSONObject(response)

                if (jsonObject.getString("status") == "OK") {
                    val routes = jsonObject.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val polyline = route.getJSONObject("overview_polyline")
                        val points = polyline.getString("points")

                        // Decode polyline to get list of LatLng points
                        val routePoints = decodePolyline(points)
                        onRouteReceived(routePoints)
                    }
                } else {
                    Log.e(
                        "getRouteForActivity",
                        "❌ Failed to get route: ${jsonObject.getString("status")}"
                    )
                }
            } catch (e: Exception) {
                Log.e("getRouteForActivity", "❌ Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateActivityStatus(activityId: Long, newStatus: String) {
        viewModelScope.launch {
            try {
                // Get the current activity
                val activity = _activities.value.find { it.activityID == activityId }
                if (activity == null) {
                    Log.e("updateActivityStatus", "❌ Activity not found with ID: $activityId")
                    return@launch
                }

                Log.d("updateActivityStatus", "Current activity: $activity")
                Log.d("updateActivityStatus", "Updating status to: $newStatus")

                // Create updated activity with new status
                val updatedActivity = activity.copy(
                    activityStatusName = newStatus,
                    activityStatusID = when (newStatus) {
                        "Planned" -> 1L
                        "Started" -> 2L
                        "Paused" -> 3L
                        "Cancelled" -> 4L
                        "Completed" -> 5L
                        else -> {
                            Log.e("updateActivityStatus", "❌ Invalid status: $newStatus")
                            return@launch
                        }
                    }
                )

                Log.d("updateActivityStatus", "Updated activity: $updatedActivity")

                // Update the activity in the repository
                repository.updateActivity(activityId, updatedActivity).collect { result ->
                    if (result != null) {
                        // Update local state
                        _activities.value = _activities.value.map {
                            if (it.activityID == activityId) updatedActivity else it
                        }
                        Log.d("updateActivityStatus", "✅ Activity status updated successfully")

                        // Show appropriate notifications based on status
                        when (newStatus) {
                            "Started" -> {
                                Log.d(
                                    "updateActivityStatus",
                                    "Showing activity started notification"
                                )
                                notificationService.showActivityStartedNotification(updatedActivity)
                            }

                            "Completed" -> {
                                Log.d(
                                    "updateActivityStatus",
                                    "Activity completed: ${updatedActivity.activityName}"
                                )
                            }

                            "Cancelled" -> {
                                Log.d(
                                    "updateActivityStatus",
                                    "Activity cancelled: ${updatedActivity.activityName}"
                                )
                            }

                            "Paused" -> {
                                Log.d(
                                    "updateActivityStatus",
                                    "Activity paused: ${updatedActivity.activityName}"
                                )
                            }

                            else -> {
                                Log.d(
                                    "updateActivityStatus",
                                    "No notification needed for status: $newStatus"
                                )
                            }
                        }
                    } else {
                        Log.e(
                            "updateActivityStatus",
                            "❌ Failed to update activity status - repository returned null"
                        )
                        Log.e("updateActivityStatus", "❌ Updated activity: $updatedActivity")
                        Log.e("updateActivityStatus", "❌ Current result: $result")
                    }
                }
            } catch (e: Exception) {
                Log.e("updateActivityStatus", "❌ Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // ! Add Activity
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

                Log.d(
                    "addActivity",
                    "✅ Adding new activity for user ${loggedInUser.userUsername}..."
                )

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
                val fromAddressWithPostcode =
                    if (fromPostcode.isNotBlank()) "$startAddressLine, $fromPostcode" else startAddressLine
                val toAddressWithPostcode =
                    if (toPostcode.isNotBlank()) "$destAddressLine, $toPostcode" else destAddressLine

                val fromCoordinates = geocodeAddress(fromAddressWithPostcode)
                val toCoordinates = geocodeAddress(toAddressWithPostcode)

                if (fromCoordinates == null || toCoordinates == null) {
                    Log.e(
                        "addActivity",
                        "❌ Could not geocode addresses - aborting activity creation"
                    )
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
                    Log.e(
                        "addActivity",
                        "❌ From location creation failed, aborting activity creation"
                    )
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
                    Log.e(
                        "addActivity",
                        "❌ To location creation failed, aborting activity creation"
                    )
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

    // ! Update Activity
    fun updateActivity(activityId: Long, updatedActivity: Activity) {
        viewModelScope.launch {
            try {
                Log.d("updateActivity", "Updating activity with ID: $activityId")
                Log.d("updateActivity", "Updated activity data: $updatedActivity")

                repository.updateActivity(activityId, updatedActivity).collect { result ->
                    if (result != null) {
                        // Update local state
                        _activities.value = _activities.value.map {
                            if (it.activityID == activityId) updatedActivity else it
                        }
                        Log.d("updateActivity", "✅ Activity updated successfully")
                    } else {
                        Log.e("updateActivity", "❌ Failed to update activity")
                    }
                }
            } catch (e: Exception) {
                Log.e("updateActivity", "❌ Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // ! Delete Activity
    fun deleteActivity(activityId: Long): Activity? {
        val existingActivity = _activities.value.find { it.activityID == activityId } ?: return null
        _activities.value -= existingActivity

        viewModelScope.launch {
            try {
                Log.d("deleteActivity", "Deleting activity with ID: $activityId")
                Log.d("deleteActivity", "Activity details: $existingActivity")

                // Delete the activity first
                repository.deleteActivity(activityId).collect { result ->
                    if (result != null) {
                        Log.d("deleteActivity", "✅ Activity deleted successfully")

                        // Delete the associated locations
                        val fromLocationId = existingActivity.activityFromID
                        val toLocationId = existingActivity.activityToID

                        Log.d("deleteActivity", "Deleting from location with ID: $fromLocationId")
                        repository.deleteLocation(fromLocationId).collect { fromLocationDeleted ->
                            if (fromLocationDeleted) {
                                Log.d("deleteActivity", "✅ From location deleted successfully")
                            } else {
                                Log.e("deleteActivity", "❌ Failed to delete from location")
                            }
                        }

                        Log.d("deleteActivity", "Deleting to location with ID: $toLocationId")
                        repository.deleteLocation(toLocationId).collect { toLocationDeleted ->
                            if (toLocationDeleted) {
                                Log.d("deleteActivity", "✅ To location deleted successfully")
                            } else {
                                Log.e("deleteActivity", "❌ Failed to delete to location")
                            }
                        }
                    } else {
                        Log.e("deleteActivity", "❌ Failed to delete activity")
                    }
                }
            } catch (e: Exception) {
                Log.e("deleteActivity", "❌ Exception: ${e.message}")
                e.printStackTrace()
            }
        }
        return existingActivity
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

                Log.d(
                    "removeContact",
                    "Removing contact relationship between $loggedInUserId and $contactUserId"
                )

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
                        Log.e("RoutesAPI", "❌ API Error - HTT`P $responseCode: $responseMessage")
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
        onResult: (List<LatLng>) -> Unit
    ) {
        viewModelScope.launch {
            getRoute(
                startPostCode,
                startAddressLine,
                destPostCode,
                destAddressLine
            ).collect { routePoints ->
                if (routePoints.isNotEmpty()) {
                    onResult(routePoints)
                } else {
                    Log.e(
                        "getRouteForLocation",
                        "❌ No route found for $startAddressLine to $destAddressLine"
                    )
                }
            }
        }
    }

    // * Other Functions
    private fun getRoute(
        startPostCode: String,
        startAddressLine: String,
        postCode: String,
        address: String,
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

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onComplete: (Boolean) -> Unit
    ) {
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
                        val updatedUserList = result.filterIsInstance<UserWithContact>()
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

    private fun loadEmergencyContacts() {
        viewModelScope.launch {
            try {
                val contacts = repository.getContacts().firstOrNull() ?: emptyList()
//                _emergencyContacts.value = contacts.filter { it.contactLabel == "Emergency" }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error loading emergency contacts: ${e.message}")
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val usersList = repository.getAllUsers().firstOrNull() ?: emptyList()
                _users.value = usersList.associateBy { it.userID }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error loading users: ${e.message}")
            }
        }
    }

    fun getUserById(userId: Long): UserWithContact? {
        return _users.value[userId]
    }

    fun getCurrentActivity(): Activity? {
        return _currentActivity.value
    }

    fun openDirections(latitude: Double, longitude: Double) {
        val uri = "google.navigation:q=$latitude,$longitude"
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
        intent.setPackage("com.google.android.apps.maps")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun updateCurrentActivity(activity: Activity?) {
        _currentActivity.value = activity
    }

    //StepCounter functionality
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private var sensorManager: SensorManager? = null
    private var sensorEventListener: SensorEventListener? = null
    private var start = -1f


    fun startStepCounting(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Log.e("StepCounter", "❌ Sensor does not exists!")
            return
        }

        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                    if (start == -1f) {
                        start = event.values[0]
                    }
                    val steps = (event.values[0] - start).toInt()
                    viewModelScope.launch {
                        _stepCount.emit(steps)
                    }
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }
        }
        sensorManager?.registerListener(
            sensorEventListener,
            stepSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun stopStepCounting(){
        sensorManager?.let{ sensorManager ->
            sensorEventListener?.let { stepListener ->
                sensorManager.unregisterListener(stepListener)
                Log.d("Step Counter", "Release the listener for the step counter")
            }

        }
        sensorManager = null
        sensorEventListener = null
        start = -1f
    }

    fun resetStepCount() {
        start = -1f
        viewModelScope.launch {
            _stepCount.emit(0)
        }
    }


    //Accelerometer functionality
    private var accelerometerSensorManager: SensorManager? = null
    private var accelerometerSensorEventListener: SensorEventListener? = null
    private var lastStepTimestamp = 0L

    fun startAccelerometerCounting(context: Context){
        accelerometerSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = accelerometerSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if(accelerometerSensor == null){
            Log.e("Accelerometer", "❌ Does not exist!")
            return
        }

        accelerometerSensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val currentTime = System.currentTimeMillis()
                val timeSinceLastStep = currentTime - lastStepTimestamp

                //After 0.5sec with the magnitude > 11, increment the counter
                if (magnitude > 11 && timeSinceLastStep > 500) {
                    lastStepTimestamp = currentTime
                    viewModelScope.launch {
                        _stepCount.emit(_stepCount.value + 1)
                    }
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }

        }
        accelerometerSensorManager?.registerListener(
            accelerometerSensorEventListener,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_UI
        )

    }

    fun stopAccelerometerCounting(){
        accelerometerSensorManager?.let { sensorManager ->
            accelerometerSensorEventListener?.let{sensorEventListener ->
                sensorManager.unregisterListener(sensorEventListener)
            }
        }
        accelerometerSensorManager = null
        accelerometerSensorEventListener = null
        lastStepTimestamp = 0L
    }

    //Release listener for the step counter
    override fun onCleared() {
        super.onCleared()
        stopStepCounting()
        stopAccelerometerCounting()
    }

//    fun sendEmergencyAlert(currentActivity: Activity?) {
//        viewModelScope.launch {
//            try {
//                val loggedInUser = _loggedInUser.value
//                if (loggedInUser == null) {
//                    Log.e("MapViewModel", "No logged in user found")
//                    return@launch
//                }
//
//                // Get FCM token for the current user
//                val fcmToken = FirebaseMessaging.getInstance().token.await()
//
//                // Send emergency alert to all emergency contacts
//                emergencyContacts.value.forEach { contact ->
//                    val emergencyData = mapOf(
//                        "type" to "emergency",
//                        "userId" to loggedInUser.userID.toString(),
//                        "panicTime" to System.currentTimeMillis().toString(),
//                        "userName" to "${loggedInUser.userFirstname} ${loggedInUser.userLastname}",
//                        "activityName" to (currentActivity?.activityName ?: "No current activity"),
//                        "latitude" to (loggedInUser.userLatitude?.toString() ?: ""),
//                        "longitude" to (loggedInUser.userLongitude?.toString() ?: "")
//                    )
//
//                    // Send to your Firebase Cloud Function
//                    repository.sendEmergencyNotification(contact.userID, emergencyData)
//                }
//            } catch (e: Exception) {
//                Log.e("MapViewModel", "Error sending emergency alert: ${e.message}")
//            }
//        }
//    }

    // Emergency Alert
    fun sendEmergencyAlert(userId: Long, data: Map<String, String>) {
        viewModelScope.launch {
            try {
                repository.sendEmergencyNotification(userId, data)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error sending emergency alert: ${e.message}")
            }
        }
    }

    private suspend fun getEmergencyContacts(userId: Long): List<UserWithContact> {
        try {
            // Get all contacts from the Contact table
            val contacts = repository.getContacts().first()
            
            // Filter contacts with "Emergency" label for the current user
            val emergencyContactIds = contacts.filter { contact ->
                contact.contactUserID == userId && 
                contact.contactLabel.equals("Emergency", ignoreCase = true)
            }.map { it.contactContactID }

            Log.d("MapViewModel", "Found ${emergencyContactIds.size} emergency contact IDs")

            // Get user information for each emergency contact
            val emergencyContacts = emergencyContactIds.mapNotNull { contactId ->
                try {
                    repository.getUserById(contactId).first().firstOrNull()
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error getting user info for contact $contactId: ${e.message}")
                    null
                }
            }

            Log.d("MapViewModel", "Found ${emergencyContacts.size} emergency contacts with user info")
            return emergencyContacts
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error getting emergency contacts: ${e.message}")
            return emptyList()
        }
    }

    fun triggerPanicAlert() {
        viewModelScope.launch {
            try {
                val currentUser = _loggedInUser.value ?: throw Exception("No logged in user found")
                
                // Get emergency contacts with their user information
                val emergencyContacts = getEmergencyContacts(currentUser.userID)
                
                if (emergencyContacts.isEmpty()) {
                    Log.w("MapViewModel", "No emergency contacts found for user ${currentUser.userID}")
                    _uiState.update { it.copy(error = "No emergency contacts found") }
                    return@launch
                }

                // Get current activity if available
                val currentActivity = _currentActivity.value

                // Send panic alert notification to each emergency contact
                emergencyContacts.forEach { contact ->
                    try {
                        // Create notification data
                        val notificationData = mapOf(
                            "type" to "emergency",
                            "userId" to currentUser.userID.toString(),
                            "userName" to "${currentUser.userFirstname} ${currentUser.userLastname}",
                            "contactId" to contact.userID.toString(),
                            "contactName" to "${contact.userFirstname} ${contact.userLastname}",
                            "timestamp" to System.currentTimeMillis().toString(),
                            "activityId" to (currentActivity?.activityID?.toString() ?: ""),
                            "activityName" to (currentActivity?.activityName ?: ""),
                            "latitude" to (currentUser.userLatitude?.toString() ?: ""),
                            "longitude" to (currentUser.userLongitude?.toString() ?: "")
                        )

                        // Send notification through repository
                        repository.sendEmergencyNotification(contact.userID, notificationData)
                        Log.d("MapViewModel", "Sent emergency notification to contact: ${contact.userID}")
                    } catch (e: Exception) {
                        Log.e("MapViewModel", "Error sending notification to contact ${contact.userID}: ${e.message}")
                    }
                }

                // Update UI state
                _uiState.update { it.copy(isPanicAlertActive = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to trigger panic alert") }
            }
        }
    }
}

data class MapUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPanicAlertActive: Boolean = false,
    val currentActivity: Activity? = null,
    val contacts: List<UserWithContact> = emptyList(),
    val currentUser: UserWithContact? = null
)
