package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("ActivityID") val activityID: Long = 0,
    @SerializedName("ActivityName") val activityName: String,
    @SerializedName("ActivityUserID") val activityUserID: Long,
    @SerializedName("ActivityDescription") val activityDescription: String,
    @SerializedName("ActivityFromID") val activityFromID: String,
    @SerializedName("ActivityLeave") val activityLeave: Long,
    @SerializedName("ActivityToID") val activityToID: String,
    @SerializedName("ActivityArrive") val activityArrive: Long,
    @SerializedName("ActivityStatusID") val activityStatusID: String,
    @SerializedName("ActivityUsername") val activityUsername: Long,
    @SerializedName("ActivityFromName") val activityFromName: Int,
    @SerializedName("ActivityToName") val activityToName: String,
    @SerializedName("ActivityStatusName") val activityStatusName: String
)
