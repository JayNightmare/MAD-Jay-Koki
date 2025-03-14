package com.example.staysafe.model.data

data class Contact(
    val contactID: Long = 0,
    val contactUserID: Long,
    val contactContactID: Long,
    val contactLabel: String,
    val contactDateCreated: Long
)
