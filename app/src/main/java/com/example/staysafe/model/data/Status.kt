package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class Status(
    @SerializedName("StatusID") val statusID: Long = 0,
    @SerializedName("StatusName") val statusName: String,
    @SerializedName("StatusOrder") val statusOrder: Int
)
