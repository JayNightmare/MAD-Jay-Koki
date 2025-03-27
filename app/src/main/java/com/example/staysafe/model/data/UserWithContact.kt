package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class UserWithContact(
    @SerializedName("UserID") val userID: Long,
    @SerializedName("UserFirstname") val userFirstname: String,
    @SerializedName("UserLastname") val userLastname: String,
    @SerializedName("UserPhone") val userPhone: String,
    @SerializedName("UserUsername") val userUsername: String,
    @SerializedName("UserPassword") val userPassword: String,
    @SerializedName("UserLatitude") val userLatitude: Double?,
    @SerializedName("UserLongitude") val userLongitude: Double?,
    @SerializedName("UserTimestamp") val userTimestamp: Long?,
    @SerializedName("UserImageURL") val userImageURL: String,
    @SerializedName("UserContactID") val userContactID: Long?,
    @SerializedName("ContactLabel") val contactLabel: String = "Friend"
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
        userTimestamp = userTimestamp ?: System.currentTimeMillis(),
        userImageURL = userImageURL
    )
}
