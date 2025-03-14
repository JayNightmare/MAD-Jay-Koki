package com.example.staysafe.repository

import com.example.staysafe.API.Service
import com.example.staysafe.model.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.await

class StaySafeRepository(
    private val service: Service
) {
    // Fetch Activities directly from API
    suspend fun getAllActivities(): Flow<List<Activity>> = flow {
        try {
            val activities = service.getAllActivities().await()
            emit(activities)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList()) // Return an empty list on failure
        }
    }

    suspend fun getActivityById(id: Long): Flow<List<Activity>> = flow {
        try {
            val activity = service.getActivity(id).await()
            emit(activity)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun getActivityByUser(userId: Long): Flow<List<Activity>> = flow {
        try {
            val activities = service.getActivityUser(userId).await()
            emit(activities)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // Fetch Locations directly from API
    suspend fun getAllLocations(): Flow<List<Location>> = flow {
        try {
            val locations = service.getAllLocations().await()
            emit(locations)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getLocationById(id: Long): Flow<List<Location>> = flow {
        try {
            val location = service.getLocation(id).await()
            emit(location)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // Fetch Positions directly from API
    suspend fun getAllPositions(): Flow<List<Position>> = flow {
        try {
            val positions = service.getAllPositions().await()
            emit(positions)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun getPositionById(id: Long): Flow<List<Position>> = flow {
        try {
            val position = service.getPositions(id).await()
            emit(position)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // Fetch Users directly from API
    suspend fun getAllUsers(): Flow<List<User>> = flow {
        try {
            val users = service.getUsers().await()
            emit(users)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun getUserById(id: Long): Flow<List<User>> = flow {
        try {
            val user = service.getUser(id).await()
            emit(user)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
}
