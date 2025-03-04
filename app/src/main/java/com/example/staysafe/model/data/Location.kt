package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userID"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ],
    indices = [Index(value = ["userID"])] // Fix: Add index to foreign key
)
data class Location(
    @PrimaryKey(autoGenerate = true) val locationID: Long = 0,
    val locationName: String,
    val locationAddress: String,
    val locationPostcode: String,
    val locationLatitude: Double,
    val locationLongitude: Double,
    val userID: Long
)
