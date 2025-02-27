package com.example.staysafe.model.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userID"],
        childColumns = ["contactUserID"],
        onDelete = CASCADE
    )]
)
data class Contact(
    @PrimaryKey(autoGenerate = true) val contactID: Long = 0,
    val contactUserID: Long,
    val contactContactID: Long,
    val contactLabel: String,
    val contactDateCreated: Long
)
