package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statuses")
data class Status(
    @PrimaryKey(autoGenerate = true) val statusID: Long = 0,
    val statusName: String,
    val statusOrder: Int
)
