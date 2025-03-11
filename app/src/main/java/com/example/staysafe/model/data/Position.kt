package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "positions",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["activityID"],
            childColumns = ["positionActivityID"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ],
    indices = [Index(value = ["positionActivityID"])] // Fix: Add index to foreign key
)
data class Position(
    @PrimaryKey(autoGenerate = true) val positionID: Long = 0,
    val positionActivityID: Long,
    val positionLatitude: Double,
    val positionLongitude: Double,
    val positionTimestamp: Long,
)
