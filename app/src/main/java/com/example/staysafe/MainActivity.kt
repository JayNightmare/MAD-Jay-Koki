package com.example.staysafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.staysafe.map.MapScreen
import com.example.staysafe.model.database.StaySafeDatabase
import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.example.staysafe.model.data.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context: Context = this

        val dummyUsers = listOf(
            User(userID = 1, userFirstname = "Alice", userLastname = "Johnson", userUsername = "alicej", userPassword = "hashed_password_1", userLatitude = 51.5074, userLongitude = -0.1278, userTimestamp = System.currentTimeMillis()),
            User(userID = 2, userFirstname = "Bob", userLastname = "Smith", userUsername = "bobsmith", userPassword = "hashed_password_2", userLatitude = 40.7128, userLongitude = -74.0060, userTimestamp = System.currentTimeMillis()),
            User(userID = 3, userFirstname = "Charlie", userLastname = "Brown", userUsername = "charlieb", userPassword = "hashed_password_3", userLatitude = 34.0522, userLongitude = -118.2437, userTimestamp = System.currentTimeMillis())
        )

        val dummyStatuses = listOf(
            Status(statusID = 1, statusName = "Active", statusOrder = 1),
            Status(statusID = 2, statusName = "Inactive", statusOrder = 2),
            Status(statusID = 3, statusName = "Pending", statusOrder = 3)
        )

        val dummyPositions = listOf(
            Position(positionID = 1, positionActivityID = 1, positionLatitude = 51.5074, positionLongitude = -0.1278, positionTimestamp = System.currentTimeMillis()),
            Position(positionID = 2, positionActivityID = 2, positionLatitude = 40.7128, positionLongitude = -74.0060, positionTimestamp = System.currentTimeMillis()),
            Position(positionID = 3, positionActivityID = 3, positionLatitude = 34.0522, positionLongitude = -118.2437, positionTimestamp = System.currentTimeMillis())
        )

        val dummyLocations = listOf(
            Location(locationID = 1, locationName = "Central Park", locationAddress = "New York, NY", locationPostcode = "10024", locationLatitude = 40.7851, locationLongitude = -73.9683),
            Location(locationID = 2, locationName = "Tower Bridge", locationAddress = "London, UK", locationPostcode = "SE1 2UP", locationLatitude = 51.5055, locationLongitude = -0.0754),
            Location(locationID = 3, locationName = "Hollywood Sign", locationAddress = "Los Angeles, CA", locationPostcode = "90068", locationLatitude = 34.1341, locationLongitude = -118.3215)
        )

        val dummyContacts = listOf(
            Contact(contactID = 1, contactUserID = 1, contactContactID = 2, contactLabel = "Friend", contactDateCreated = System.currentTimeMillis()),
            Contact(contactID = 2, contactUserID = 2, contactContactID = 3, contactLabel = "Colleague", contactDateCreated = System.currentTimeMillis()),
            Contact(contactID = 3, contactUserID = 3, contactContactID = 1, contactLabel = "Family", contactDateCreated = System.currentTimeMillis())
        )

        val dummyActivities = listOf(
            Activity(activityID = 1, activityName = "Running", activityUserID = 1, activityUserDescription = "Morning Run", activityUsername = "alicej", activityDescription = "5km run in the park", activityFromID = 1, activityToID = 2, activityDate = System.currentTimeMillis(), activityStatusID = 1),
            Activity(activityID = 2, activityName = "Cycling", activityUserID = 2, activityUserDescription = "Evening Cycle", activityUsername = "bobsmith", activityDescription = "10km cycling route", activityFromID = 2, activityToID = 3, activityDate = System.currentTimeMillis(), activityStatusID = 2),
            Activity(activityID = 3, activityName = "Hiking", activityUserID = 3, activityUserDescription = "Mountain Hike", activityUsername = "charlieb", activityDescription = "Hiking in the mountains", activityFromID = 3, activityToID = 1, activityDate = System.currentTimeMillis(), activityStatusID = 3)
        )

        lifecycleScope.launch {
            val db = StaySafeDatabase.getDatabase(context)

            db.userDao().insertUser(dummyUsers[0])
            db.userDao().insertUser(dummyUsers[1])
            db.userDao().insertUser(dummyUsers[2])

            db.statusDao().insertStatus(dummyStatuses[0])
            db.statusDao().insertStatus(dummyStatuses[1])
            db.statusDao().insertStatus(dummyStatuses[2])

            db.positionDao().insertPosition(dummyPositions[0])
            db.positionDao().insertPosition(dummyPositions[1])
            db.positionDao().insertPosition(dummyPositions[2])

            db.locationDao().insertLocation(dummyLocations[0])
            db.locationDao().insertLocation(dummyLocations[1])
            db.locationDao().insertLocation(dummyLocations[2])

            db.contactDao().insertContact(dummyContacts[0])
            db.contactDao().insertContact(dummyContacts[1])
            db.contactDao().insertContact(dummyContacts[2])

            db.activityDao().insertActivity(dummyActivities[0])
            db.activityDao().insertActivity(dummyActivities[1])
            db.activityDao().insertActivity(dummyActivities[2])
        }

        setContent {
            MapScreen()
        }
    }
}
