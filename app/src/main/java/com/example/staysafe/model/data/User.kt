package com.example.staysafe.model.data

data class User(
    val userID: Long = 0,
    val userFirstname: String,
    val userLastname: String,
    val userPhone: String,
    val userUsername: String,
    val userPassword: String,
    val userLatitude: Double?,
    val userLongitude: Double?,
    val userTimestamp: Long?
)