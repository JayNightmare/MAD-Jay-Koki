package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity",
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
data class Activity(
    @PrimaryKey(autoGenerate = true) val activityID: Long = 0,
    val activityName: String,
    val activityUserID: Long,
    val activityUserDescription: String,
    val activityUsername: String,
    val activityDescription: String,
    val activityFromID: Long,
    val activityToID: Long,
    val activityDate: Long,
    val activityStatusID: Int
)
