package com.example.staysafe.viewModel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staysafe.model.data.*
import com.example.staysafe.repository.StaySafeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: StaySafeRepository
) : ViewModel() {

    // Users
    private val _users = MutableStateFlow<List<User>>(emptyList())
    @SuppressLint("RestrictedApi")
    val users: StateFlow<List<User>> = _users

    private fun fetchAllUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { _users.value = it }
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

    fun fetchLocationById(id: Long): StateFlow<List<Location>> {
        return repository.getLocationById(id)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
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
}
