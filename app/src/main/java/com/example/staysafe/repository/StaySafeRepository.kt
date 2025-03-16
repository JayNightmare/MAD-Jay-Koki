package com.example.staysafe.repository

import android.util.Log
import com.example.staysafe.API.Service
import com.example.staysafe.model.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.await
import retrofit2.awaitResponse

class StaySafeRepository(
    private val service: Service
) {
    // //
    // * Activity

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
    // //

    // //
    // * Locations

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

    fun getLocationById(id: Long): Flow<List<Location>> {
        return flow {
            println("DEBUG: getLocationById() CALLED")  // ✅ Log when function is called
            println("DEBUG: id: $id")  // ✅ Log the value of id

            val response = service.getLocation(id).awaitResponse()

            if (response.isSuccessful) {
                val locations = response.body() ?: emptyList()
                println("DEBUG: Received locations: $locations")  // ✅ Log the received locations
                emit(locations)
            } else {
                println("DEBUG: API returned error: ${response.errorBody()?.string()}")  // Log API error
                emit(emptyList()) // Emit an empty list if API fails
            }
        }.catch { e ->
            println("DEBUG: Exception: ${e.message}")  // ✅ Log any exceptions
            e.printStackTrace()
            emit(emptyList()) // Emit empty list safely inside `catch`
        }.flowOn(Dispatchers.IO) // Ensures the API call runs on background thread
    }
    // //

    // //
    // * Positions

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
    // //

    // //
    // * Status
    suspend fun getStatus(): Flow<List<Status>> = flow {
        try {
            val statusList = service.getStatus().awaitResponse()
            emit(statusList.body() ?: emptyList())
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // //

    // //
    // * Users

    // Fetch Users directly from API
    suspend fun getAllUsers(): Flow<List<User>> = flow {
        try {
            val users = service.getUsers().awaitResponse()
            println("DEBUG: API call executed, response received: $users")
            users.body()?.let {
                println("DEBUG: Users body found, emitting data: $it")
                emit(it)
            }
            println("DEBUG: Works! Users data: ${users.body()}")
        } catch (e: Exception) {
            println("API_ERROR: Exception - ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun getUserById(id: Long): Flow<List<User>> = flow {
        try {
            val user = service.getUser(id).awaitResponse()
            user.body()?.let { emit(it) }?:emit(emptyList())
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
    // //

    // //
    // * Contacts
//    suspend fun getUserContacts(userId: Long): Flow<List<Contact>> = flow {
//        try {
//            val contacts = service.getUserContact(userId).awaitResponse()
//            contacts.body()?.let { emit(it) } ?: emit(emptyList())
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emit(emptyList())
//        }
//    }

    suspend fun getContactsForUser(userId: Long): Flow<List<User>> = flow {
        try {
            Log.d("MapViewModel", "Fetching user contacts for userId: $userId")
            val contacts = service.getUserContact(userId).awaitResponse()
            Log.d("MapViewModel", "Received contacts: ${contacts.body()}")
            val contactIds = contacts.body()?.map { it.userID } ?: emptyList()

            val allUsers = service.getUsers().awaitResponse()
            val filteredUsers = allUsers.body()?.filter { it.userID in contactIds } ?: emptyList()
            Log.d("MapViewModel", "Filtered users: $filteredUsers")

            emit(filteredUsers)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
    // //
}
