package com.example.staysafe.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.staysafe.model.dao.LocationDao
import com.example.staysafe.model.dao.UserDao
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.User
import com.example.staysafe.model.database.StaySafeDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PeopleViewModel(application: Application):AndroidViewModel(application) {
    private val userDao :UserDao = StaySafeDatabase.getDatabase(application).userDao()
    private val locationDao: LocationDao = StaySafeDatabase.getDatabase(application).locationDao()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    private val _username = MutableStateFlow("")
    val usernames: StateFlow<String> = _username

    private val _userLongitude = MutableStateFlow(0.0)
    val userLongitude: StateFlow<Double> = _userLongitude

    private val _locationName = MutableStateFlow("")
    val locationName : StateFlow<String> = _locationName

    private val _locations = MutableStateFlow<Map<String, Location>>(emptyMap())
    val locations: StateFlow<Map<String, Location>> = _locations

    fun loadUsers() {
        viewModelScope.launch {
            val userList = userDao.getAllUsers()
            _users.value = userList

            val locationList = locationDao.getAllLocations()
            val locationMap = locationList.associateBy { it.locationName }

            _locations.value = locationMap
        }
    }

}