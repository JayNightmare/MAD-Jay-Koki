package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "positions")
data class Position(
    @PrimaryKey(autoGenerate = true) val positionID: Long = 0,
    val positionActivityID: Long,
    val positionLatitude: Double,
    val positionLongitude: Double,
    val positionTimestamp: Long
)
