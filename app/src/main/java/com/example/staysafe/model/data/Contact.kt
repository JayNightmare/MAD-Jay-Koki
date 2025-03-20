package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class Contact(
    @SerializedName("ContactID") val contactID: Long = 0,
    @SerializedName("ContactUserID") val contactUserID: Long,
    @SerializedName("ContactContactID") val contactContactID: Long,
    @SerializedName("ContactLabel") val contactLabel: String,
    @SerializedName("ContactDatecreated") val contactDateCreated: String
)
