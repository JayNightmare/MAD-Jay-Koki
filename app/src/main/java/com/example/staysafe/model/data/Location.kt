package com.example.staysafe.model.data

data class Location(
    val locationID: Long = 0,
    val locationName: String,
    val locationDescription: String,
    val locationAddress: String,
    val locationPostcode: String,
    val locationLatitude: Double,
    val locationLongitude: Double,
)
