package com.example.staysafe.model.data

data class Activity(
    val activityID: Long = 0,
    val activityName: String,
    val activityUserID: Long,
    val activityUsername: String,
    val activityDescription: String,
    val activityFromID: Long,
    val activityFromName: String,
    val activityToID: Long,
    val activityToName: String,
    val activityDate: Long,
    val activityStatusID: Int,
    val activityStatusName: String
)

