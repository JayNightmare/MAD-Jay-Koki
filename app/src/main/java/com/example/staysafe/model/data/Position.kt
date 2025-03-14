package com.example.staysafe.model.data


data class Position(
    val positionID: Long = 0,
    val positionActivityID: Long,
    val positionActivityName: String,
    val positionLatitude: Double,
    val positionLongitude: Double,
    val positionTimestamp: Long,
)
