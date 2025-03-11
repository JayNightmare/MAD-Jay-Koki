package com.example.staysafe.model.dummyData

import com.example.staysafe.MainActivity
import com.example.staysafe.model.data.Activity
import com.example.staysafe.model.data.Contact
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.Position
import com.example.staysafe.model.data.Status
import com.example.staysafe.model.data.User
import com.example.staysafe.model.database.StaySafeDatabase
import com.example.staysafe.API.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import com.example.staysafe.viewModel.MapViewModel
import kotlinx.coroutines.launch

class DatabaseSeeder {
    companion object {
        private val dummyUsers = listOf(
            User(userID = 1, userFirstname = "Alice", userLastname = "Johnson", userUsername = "alicej", userPassword = "hashed_password_1", userLatitude = 51.5074, userLongitude = -0.1278, userTimestamp = System.currentTimeMillis()),
            User(userID = 2, userFirstname = "Bob", userLastname = "Smith", userUsername = "bobsmith", userPassword = "hashed_password_2", userLatitude = 40.7128, userLongitude = -74.0060, userTimestamp = System.currentTimeMillis()),
            User(userID = 3, userFirstname = "Charlie", userLastname = "Brown", userUsername = "charlieb", userPassword = "hashed_password_3", userLatitude = 34.0522, userLongitude = -118.2437, userTimestamp = System.currentTimeMillis()),

            User(userID = 4, userFirstname = "David", userLastname = "Lee", userUsername = "davidlee", userPassword = "hashed_password_4", userLatitude = 51.5074, userLongitude = -0.1278, userTimestamp = System.currentTimeMillis()),
            User(userID = 5, userFirstname = "Eve", userLastname = "Williams", userUsername = "evew", userPassword = "hashed_password_5", userLatitude = 40.7128, userLongitude = -74.0060, userTimestamp = System.currentTimeMillis()),
            User(userID = 6, userFirstname = "Frank", userLastname = "Miller", userUsername = "frankm", userPassword = "hashed_password_6", userLatitude = 34.0522, userLongitude = -118.2437, userTimestamp = System.currentTimeMillis()),
        )

        private val dummyStatuses = listOf(
            Status(statusID = 1, statusName = "Active", statusOrder = 1),
            Status(statusID = 2, statusName = "Inactive", statusOrder = 2),
            Status(statusID = 3, statusName = "Pending", statusOrder = 3),

            Status(statusID = 4, statusName = "Active", statusOrder = 1),
            Status(statusID = 5, statusName = "Inactive", statusOrder = 2),
            Status(statusID = 6, statusName = "Pending", statusOrder = 3),
        )

        private val dummyPositions = listOf(
            Position(positionID = 1, positionActivityID = 1, positionLatitude = 51.5074, positionLongitude = -0.1278, positionTimestamp = System.currentTimeMillis()),
            Position(positionID = 2, positionActivityID = 2, positionLatitude = 40.7128, positionLongitude = -74.0060, positionTimestamp = System.currentTimeMillis()),
            Position(positionID = 3, positionActivityID = 3, positionLatitude = 34.0522, positionLongitude = -118.2437, positionTimestamp = System.currentTimeMillis()),

            Position(positionID = 4, positionActivityID = 1, positionLatitude = 51.5074, positionLongitude = -0.1278, positionTimestamp = System.currentTimeMillis()),
            Position(positionID = 5, positionActivityID = 2, positionLatitude = 40.7128, positionLongitude = -74.0060, positionTimestamp = System.currentTimeMillis()),
            Position(positionID = 6, positionActivityID = 3, positionLatitude = 34.0522, positionLongitude = -118.2437, positionTimestamp = System.currentTimeMillis()),
        )

        private val dummyLocations = listOf(
            Location(locationID = 1, locationName = "Central Park", locationAddress = "New York, NY", locationPostcode = "10024", locationLatitude = 40.7851, locationLongitude = -73.9683, userID = 1),
            Location(locationID = 2, locationName = "Tower Bridge", locationAddress = "London, UK", locationPostcode = "SE1 2UP", locationLatitude = 51.5055, locationLongitude = -0.0754, userID = 2),
            Location(locationID = 3, locationName = "Hollywood Sign", locationAddress = "Los Angeles, CA", locationPostcode = "90068", locationLatitude = 34.1341, locationLongitude = -118.3215, userID = 3),

            Location(locationID = 4, locationName = "Trafalgar Square", locationAddress = "London, UK", locationPostcode = "WC2N 5DS", locationLatitude = 40.7851, locationLongitude = -73.9683, userID = 4),
            Location(locationID = 5, locationName = "Tower Bridge", locationAddress = "London, UK", locationPostcode = "SE1 2UP", locationLatitude = 51.5055, locationLongitude = -0.0754, userID = 5),
            Location(locationID = 6, locationName = "Hollywood Sign", locationAddress = "Los Angeles, CA", locationPostcode = "90068", locationLatitude = 34.1341, locationLongitude = -118.3215, userID = 6),
        )


        private val dummyContacts = listOf(
            Contact(contactID = 1, contactUserID = 1, contactContactID = 2, contactLabel = "Friend", contactDateCreated = System.currentTimeMillis()),
            Contact(contactID = 2, contactUserID = 2, contactContactID = 3, contactLabel = "Colleague", contactDateCreated = System.currentTimeMillis()),
            Contact(contactID = 3, contactUserID = 3, contactContactID = 1, contactLabel = "Family", contactDateCreated = System.currentTimeMillis()),
            Contact(contactID = 4, contactUserID = 4, contactContactID = 3, contactLabel = "Friend", contactDateCreated = System.currentTimeMillis()),
            Contact(contactID = 5, contactUserID = 5, contactContactID = 1, contactLabel = "Colleague", contactDateCreated = System.currentTimeMillis()),
            Contact(contactID = 6, contactUserID = 6, contactContactID = 2, contactLabel = "Family", contactDateCreated = System.currentTimeMillis()),
        )

        private val dummyActivities = listOf(
            Activity(activityID = 1, activityName = "Running", activityUserID = 1, activityUserDescription = "Morning Run", activityUsername = "alicej", activityDescription = "5km run in the park", activityFromID = 1, activityToID = 2, activityDate = System.currentTimeMillis(), activityStatusID = 1),
            Activity(activityID = 2, activityName = "Cycling", activityUserID = 2, activityUserDescription = "Evening Cycle", activityUsername = "bobsmith", activityDescription = "10km cycling route", activityFromID = 2, activityToID = 3, activityDate = System.currentTimeMillis(), activityStatusID = 2),
            Activity(activityID = 3, activityName = "Hiking", activityUserID = 3, activityUserDescription = "Mountain Hike", activityUsername = "charlieb", activityDescription = "Hiking in the mountains", activityFromID = 3, activityToID = 1, activityDate = System.currentTimeMillis(), activityStatusID = 3)
        )


        fun insertDummyData(database: StaySafeDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                database.userDao().insertAll(dummyUsers)
                database.statusDao().insertAll(dummyStatuses)
//                database.positionDao().insertAll(dummyPositions)
                database.locationDao().insertAll(dummyLocations)
                database.contactDao().insertAll(dummyContacts)
                database.activityDao().insertAll(dummyActivities)
            }
        }
    }
}
