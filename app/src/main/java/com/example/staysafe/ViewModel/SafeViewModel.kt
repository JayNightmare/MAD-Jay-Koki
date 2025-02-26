package com.example.staysafe.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.staysafe.repository.StaySafeRepository
import com.example.staysafe.service.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Activity(
    val id: String? = null,
    val name: String,
    val startLocation: LatLng,
    val endLocation: LatLng,
    val eta: String
)

data class Contact(
    val id: String,
    val name: String,
    val phone: String
)

enum class TripStatus {
    IDLE, ACTIVE, COMPLETED, ERROR
}

class SafeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StaySafeRepository()
    private val locationService = LocationService(application)

    // Search query for route search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Current location
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    // Trip status
    private val _tripStatus = MutableStateFlow<TripStatus>(TripStatus.IDLE)
    val tripStatus: StateFlow<TripStatus> = _tripStatus

    // Contacts list
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    // Current activity/trip
    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity

    // Error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadContacts()
        if (locationService.hasLocationPermission()) {
            startLocationUpdates()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startTrip(activity: Activity) {
        viewModelScope.launch {
            _tripStatus.value = TripStatus.ACTIVE
            repository.createActivity(activity)
                .onSuccess { createdActivity ->
                    _currentActivity.value = createdActivity
                    startLocationUpdates()
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to start trip: ${error.message}"
                    _tripStatus.value = TripStatus.ERROR
                }
        }
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            try {
                locationService.getLocationUpdates()
                    .catch { e ->
                        _errorMessage.value = "Location error: ${e.message}"
                    }
                    .collect { location ->
                        _currentLocation.value = location
                        checkRouteDeviation(location)
                        updateLocationInRepository(location)
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start location updates: ${e.message}"
            }
        }
    }

    private suspend fun checkRouteDeviation(location: LatLng) {
        _currentActivity.value?.let { activity ->
            val isDeviated = repository.checkRouteDeviation(
                location,
                activity.startLocation,
                activity.endLocation
            )
            if (isDeviated) {
                // TODO: Implement deviation alert logic
                _errorMessage.value = "Warning: You have deviated from your planned route"
            }
        }
    }

    private suspend fun updateLocationInRepository(location: LatLng) {
        _currentActivity.value?.id?.let { activityId ->
            repository.updateLocation(activityId, location)
                .onFailure { error ->
                    _errorMessage.value = "Failed to update location: ${error.message}"
                }
        }
    }

    fun completeTrip() {
        viewModelScope.launch {
            _currentActivity.value?.id?.let { activityId ->
                repository.updateActivityStatus(activityId, "completed")
                    .onSuccess {
                        _tripStatus.value = TripStatus.COMPLETED
                        _currentActivity.value = null
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Failed to complete trip: ${error.message}"
                    }
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            repository.getContacts()
                .catch { error ->
                    _errorMessage.value = "Failed to load contacts: ${error.message}"
                }
                .collect { contactsList ->
                    _contacts.value = contactsList
                }
        }
    }

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            repository.addContact(contact)
                .onSuccess { newContact ->
                    _contacts.value = _contacts.value + newContact
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to add contact: ${error.message}"
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
