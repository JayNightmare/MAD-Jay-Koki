package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userID: Long = 0,
    val userFirstname: String,
    val userLastname: String,
    val userUsername: String,
    val userPassword: String,  // Store as a hashed string
    val userLatitude: Double?,
    val userLongitude: Double?,
    val userTimestamp: Long?
)
