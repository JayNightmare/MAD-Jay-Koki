package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class UserWithContact(
    @SerializedName("userID")
    val userID: Long,
    @SerializedName("userFirstname")
    val userFirstname: String,
    @SerializedName("userLastname")
    val userLastname: String,
    @SerializedName("userPhone")
    val userPhone: String,
    @SerializedName("userUsername")
    val userUsername: String,
    @SerializedName("userPassword")
    val userPassword: String,
    @SerializedName("userLatitude")
    val userLatitude: Double?,
    @SerializedName("userLongitude")
    val userLongitude: Double?,
    @SerializedName("userTimestamp")
    val userTimestamp: Long,
    @SerializedName("userImageURL")
    val userImageURL: String,
    @SerializedName("userContactID")
    val userContactID: Long
) {
    fun toUser(): User = User(
        userID = userID,
        userFirstname = userFirstname,
        userLastname = userLastname,
        userPhone = userPhone,
        userUsername = userUsername,
        userPassword = userPassword,
        userLatitude = userLatitude,
        userLongitude = userLongitude,
        userTimestamp = userTimestamp,
        userImageURL = userImageURL
    )
} 