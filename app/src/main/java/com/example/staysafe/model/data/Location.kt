package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true) val locationID: Long = 0,
    val locationName: String,
    val locationAddress: String,
    val locationPostcode: String,
    val locationLatitude: Double,
    val locationLongitude: Double
)
