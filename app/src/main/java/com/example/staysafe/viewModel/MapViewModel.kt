package com.example.staysafe.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staysafe.API.*
import com.example.staysafe.model.dao.*
import com.example.staysafe.model.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import retrofit2.Call

class MapViewModel(
    val userDao: UserDao,
    private val locationDao: LocationDao,
    private val service: Service
) : ViewModel() {

    val users = userDao.getAllUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun getLocationForUser(userId: Long): Flow<Location?> {
        return locationDao.getLocationByUserId(userId)
    }

    fun getTestingData(): Call<List<Location>> {
        return service.getAllLocations()
    }
}
