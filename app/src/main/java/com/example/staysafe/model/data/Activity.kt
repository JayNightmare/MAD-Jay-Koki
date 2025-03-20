package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("ActivityID") val activityID: Long = 0,
    @SerializedName("ActivityName") val activityName: String,
    @SerializedName("ActivityUserID") val activityUserID: Long = 0,
    @SerializedName("ActivityUserUsername") val activityUserUsername: String,
    @SerializedName("ActivityDescription") val activityDescription: String,
    @SerializedName("ActivityFromID") val activityFromID: Long = 0,
    @SerializedName("ActivityFromName") val activityFromName: String,
    @SerializedName("ActivityLeave") val activityLeave: String,
    @SerializedName("ActivityToID") val activityToID: Long = 0,
    @SerializedName("ActivityToName") val activityToName: String,
    @SerializedName("ActivityArrive") val activityArrive: String,
    @SerializedName("ActivityStatusID") val activityStatusID: Long,
    @SerializedName("ActivityStatusName") val activityStatusName: String
)
