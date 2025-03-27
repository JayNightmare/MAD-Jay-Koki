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
    fun getAllActivities(): Flow<List<Activity>> = flow {
        try {
            val activities = service.getAllActivities().await()
            emit(activities)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList()) // Return an empty list on failure
        }
    }

    fun getLatestActivityForUser(userId: Long): Flow<Activity?> = flow {
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

    fun getActivitiesForUser(userId: Long): Flow<List<Activity>> = flow {
        try {
            val response = service.getUserActivities(userId).awaitResponse()

            if (response.isSuccessful) {
                response.body()?.let { emit(it) } ?: emit(emptyList())
            } else {
                Log.e(
                    "getActivitiesForUser",
                    "❌ Error fetching activities: ${response.errorBody()?.string()}"
                )
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("getActivitiesForUser", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    fun addActivity(activity: Activity): Flow<Activity?> = flow {
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

    fun deleteActivity(activityId: Long): Flow<Any> = flow {
        try {
            val response = service.deleteActivity(activityId).awaitResponse()
            Log.d("Delete activity", "Response: $response")

            if (response.isSuccessful) {
                response.body()?.let { emit(it) } ?: emit(activityId)
            } else {
                Log.d(
                    "Delete activity",
                    "Error deleting activity: ${response.errorBody()?.string()}"
                )
                emit(activityId)
            }
        } catch (e: Exception) {
            Log.e("Delete activity", "Exception: ${e.message}")
            e.printStackTrace()
            emit(activityId)
        }
    }.flowOn(Dispatchers.IO)

    fun updateActivity(activityId: Long, activity: Activity): Flow<List<Activity>?> = flow {
        try {
            Log.d("updateActivity", "Attempting to update activity with ID: $activityId")
            Log.d("updateActivity", "Activity data: $activity")
            
            // First, try to get the current activity state
            val getResponse = service.getActivity(activityId).awaitResponse()
            Log.d("updateActivity", "Current activity state: ${getResponse.body()?.firstOrNull()}")
            
            // Now try to update the activity
            val response = service.updateActivity(activityId, activity).awaitResponse()
            Log.d("updateActivity", "Update response code: ${response.code()}")
            Log.d("updateActivity", "Update response body: ${response.body()}")
            Log.d("updateActivity", "Update response error body: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val updatedActivities = response.body()
                if (updatedActivities != null) {
                    Log.d("updateActivity", "✅ Activity updated successfully")
                    emit(updatedActivities)
                } else {
                    Log.e("updateActivity", "❌ Response body is null")
                    // Try to get the updated activity directly
                    val getUpdatedResponse = service.getActivity(activityId).awaitResponse()
                    if (getUpdatedResponse.isSuccessful) {
                        val currentActivity = getUpdatedResponse.body()?.firstOrNull()
                        if (currentActivity != null) {
                            Log.d("updateActivity", "✅ Retrieved current activity state")
                            emit(listOf(currentActivity))
                        } else {
                            Log.e("updateActivity", "❌ Could not retrieve updated activity")
                            emit(null)
                        }
                    } else {
                        Log.e("updateActivity", "❌ Failed to retrieve updated activity: ${getUpdatedResponse.errorBody()?.string()}")
                        emit(null)
                    }
                }
            } else {
                Log.e("updateActivity", "❌ Failed to update activity: ${response.errorBody()?.string()}")
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("updateActivity", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    // //

    // //
    // * Locations

    // Fetch Locations directly from API
    fun getAllLocations(): Flow<List<Location>> = flow {
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

    fun addLocation(location: Location): Flow<List<Location>?> = flow {
        try {
            Log.d("addLocation", "Adding location: $location")
            val response = service.addLocation(location).awaitResponse()

            if (response.isSuccessful) {
                Log.d("addLocation", "Location added successfully")
                emit(response.body())
            } else {
                Log.e("addLocation", "Failed to add location: ${response.errorBody()?.string()}")
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("addLocation", "Exception adding location: ${e.message}")
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getMaxLocationId(): Flow<Int> = flow {
        try {
            val locations = service.getAllLocations().await()
            val maxId = locations.maxOfOrNull { it.locationID } ?: 0
            Log.d("getMaxLocationId", "Maximum location ID from API: $maxId")
            emit(maxId)
        } catch (e: Exception) {
            Log.e("getMaxLocationId", "Error getting max location ID: ${e.message}")
            e.printStackTrace()
            emit(0) // Default to 0 if there's an error
        }
    }.flowOn(Dispatchers.IO)

    fun deleteLocation(locationId: Long): Flow<Boolean> = flow {
        try {
            val response = service.deleteLocation(locationId).awaitResponse()
            Log.d("deleteLocation", "Response: $response")

            if (response.isSuccessful) {
                Log.d("deleteLocation", "✅ Location deleted successfully")
                emit(true)
            } else {
                Log.e("deleteLocation", "❌ Error deleting location: ${response.errorBody()?.string()}")
                emit(false)
            }
        } catch (e: Exception) {
            Log.e("deleteLocation", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
    // //

    // //
    // * Positions

    // Fetch Positions directly from API
    fun getAllPositions(): Flow<List<Position>> = flow {
        try {
            val positions = service.getAllPositions().await()
            emit(positions)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getPositionById(id: Long): Flow<List<Position>> = flow {
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
    fun getStatus(): Flow<List<Status>> = flow {
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
    fun getAllUsers(): Flow<List<UserWithContact>> = flow {
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

    fun getUserById(id: Long): Flow<List<UserWithContact>> = flow {
        try {
            val user = service.getUser(id).awaitResponse()
            user.body()?.let { emit(it) } ?: emit(emptyList())
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun addUser(user: UserWithContact): Flow<Any> = flow {
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

    fun deleteUser(userID: Long): Flow<Any> = flow {
        try {
            val response = service.deleteUser(userID).awaitResponse()
            Log.d("Delete user", "Response: $response")

            if (response.isSuccessful) {
                response.body()?.let { emit(it) } ?: emit(userID)
            } else {
                Log.d("Delete user", "Error deleting user: ${response.errorBody()?.string()}")
                emit(userID)
            }
        } catch (e: Exception) {
            Log.e("Delete user", "Exception: ${e.message}")
            e.printStackTrace()
            emit(userID)
        }
    }.flowOn(Dispatchers.IO)

    fun updateUser(user: UserWithContact): Flow<Any> = flow {
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

    fun addContact(contact: Contact): Flow<Contact?> = flow {
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

    fun deleteContact(contactId: Long?): Flow<Boolean> = flow {
        try {
            val response = service.deleteContact(contactId).awaitResponse()
            Log.d("deleteContact", "Response: $response")

            if (response.isSuccessful) {
                Log.d("deleteContact", "✅ Contact deleted successfully")
                emit(true)
            } else {
                Log.e(
                    "deleteContact",
                    "❌ Error deleting contact: ${response.errorBody()?.string()}"
                )
                emit(false)
            }
        } catch (e: Exception) {
            Log.e("deleteContact", "❌ Exception: ${e.message}")
            e.printStackTrace()
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun getContactsForUser(userId: Long): Flow<List<UserWithContact>> = flow {
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

    fun getContacts(): Flow<List<Contact>> = flow {
        try {
            val contacts = service.getContacts().awaitResponse()
            emit(contacts.body() ?: emptyList())
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
    // //
}
