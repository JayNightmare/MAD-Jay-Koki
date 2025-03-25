package com.example.staysafe.repository

import android.util.Log
import com.example.staysafe.API.Service
import com.example.staysafe.model.data.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.json.JSONObject
import retrofit2.await
import retrofit2.awaitResponse
import java.net.URL

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

    suspend fun getLatestActivityForUser(userId: Long): Flow<Activity?> = flow {
        try {
            Log.d("StaySafeRepository", "Fetching user activities for userId: $userId")
            val activities = service.getUserActivities(userId).awaitResponse()
            Log.d("StaySafeRepository", "Activities: ${activities.body()}")
            val latestActivity = activities.body()?.maxByOrNull { it.activityArrive }
            Log.d("StaySafeRepository", "Latest Activity: $latestActivity")
            emit(latestActivity)
        } catch (e: Exception) {
            Log.e("StaySafeRepository", "Error fetching latest activity: ${e.message}")
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getLatestPositionForUser(userId: Long): Flow<Position?> = flow {
        try {
            val latestActivity = getLatestActivityForUser(userId).firstOrNull()

            if (latestActivity != null) {
                Log.d("StaySafeRepository", "Latest Activity: $latestActivity")

                val positions =
                    service.getActivityPositions(latestActivity.activityID).awaitResponse()
                val latestPosition = positions.body()?.maxByOrNull { it.positionTimestamp }
                emit(latestPosition)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getActivitiesForUser(userId: Long): Flow<List<Activity>> = flow {
        try {
            val response = service.getUserActivities(userId).awaitResponse()

            if (response.isSuccessful) {
                response.body()?.let { emit(it) } ?: emit(emptyList())
            } else {
                Log.e("getActivitiesForUser", "❌ Error fetching activities: ${response.errorBody()?.string()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("getActivitiesForUser", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun addActivity(activity: Activity): Flow<Activity?> = flow {
        try {
            val response = service.addActivities(activity).awaitResponse()

            if (response.isSuccessful) {
                response.body()?.let { emit(it.firstOrNull()) } ?: emit(null)
                Log.d("addActivity", "✅ Activity added successfully! Response: $response")
            } else {
                Log.e("addActivity", "❌ Error adding activity: ${response.errorBody()?.string()}")
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("addActivity", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)
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
                println(
                    "DEBUG: API returned error: ${
                        response.errorBody()?.string()
                    }"
                )  // Log API error
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
            user.body()?.let { emit(it) } ?: emit(emptyList())
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    suspend fun addUser(user: User): Flow<Any> = flow {
        try {
            val response = service.addUser(user).awaitResponse()
            Log.d("addUser", "Response: $response")

            if (response.isSuccessful) {
                response.body()?.let { emit(it) } ?: emit(user)
            } else {
                Log.e("addUser", "Error adding user: ${response.errorBody()?.string()}")
                emit(user)
            }
        } catch (e: Exception) {
            Log.e("addUser", "Exception: ${e.message}")
            e.printStackTrace()
            emit(user)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteUser (userID: Long): Flow<Any> = flow {
        try {
            val response = service.deleteUser(userID).awaitResponse()
            Log.d("Delete user", "Response: $response")

            if (response.isSuccessful){
                response.body()?.let { emit(it) } ?: emit(userID)
            }
            else{
                Log.d("Delete user","Error deleting user: ${response.errorBody()?.string()}" )
                emit(userID)
            }
        }catch (e:Exception){
            Log.e("Delete user", "Exception: ${e.message}")
            e.printStackTrace()
            emit(userID)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun updateUser(user: User): Flow<Any> = flow {
        try {
            Log.d("updateUser", "Attempting to update user: ${user.userUsername}")
            Log.d("updateUser", "User ID: ${user.userID}")
            
            val response = service.updateUser(user.userID, user).awaitResponse()
            Log.d("updateUser", "Response code: ${response.code()}")
            Log.d("updateUser", "Response body: ${response.body()}")
            Log.d("updateUser", "Response error body: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                response.body()?.let { 
                    Log.d("updateUser", "✅ User updated successfully")
                    emit(it) 
                } ?: run {
                    Log.e("updateUser", "❌ Response body is null")
                    emit(user)
                }
            } else {
                Log.e("updateUser", "❌ Error updating user: ${response.errorBody()?.string()}")
                emit(user)
            }
        } catch (e: Exception) {
            Log.e("updateUser", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(user)
        }
    }.flowOn(Dispatchers.IO)
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

    suspend fun addContact(contact: Contact): Flow<Contact?> = flow {
        try {
            val response = service.addContact(contact).awaitResponse()

            if (response.isSuccessful) {
                response.body()?.let { emit(it.firstOrNull()) } ?: emit(null)
            } else {
                Log.e("addContact", "❌ Error adding contact: ${response.errorBody()?.string()}")
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("addContact", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteContact(contactId: Long): Flow<Boolean> = flow {
        try {
            val response = service.deleteContact(contactId).awaitResponse()
            Log.d("deleteContact", "Response: $response")

            if (response.isSuccessful) {
                Log.d("deleteContact", "✅ Contact deleted successfully")
                emit(true)
            } else {
                Log.e("deleteContact", "❌ Error deleting contact: ${response.errorBody()?.string()}")
                emit(false)
            }
        } catch (e: Exception) {
            Log.e("deleteContact", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getContactsForUser(userId: Long): Flow<List<UserWithContact>> = flow {
        try {
            Log.d("MapViewModel", "Fetching user contacts for userId: $userId")
            val contacts = service.getUserContact(userId).awaitResponse()
            Log.d("MapViewModel", "Received contacts: ${contacts.body()}")
            
            emit(contacts.body() ?: emptyList())
        } catch (e: Exception) {
            Log.e("MapViewModel", "Error fetching contacts: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    // //
}
