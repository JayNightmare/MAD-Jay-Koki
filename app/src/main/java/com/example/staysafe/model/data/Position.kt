package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class Position(
    @SerializedName("PositionID") val positionID: Long = 0,
    @SerializedName("PositionActivityID") val positionActivityID: Long,
    @SerializedName("PositionLatitude") val positionLatitude: String,
    @SerializedName("PositionLongitude") val positionLongitude: Double,
    @SerializedName("PositionTimestamp") val positionTimestamp: Double,
    @SerializedName("PositionActivityName") val positionActivityName: Long,
)
