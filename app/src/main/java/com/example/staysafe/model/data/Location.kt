package com.example.staysafe.model.data

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("LocationID") val locationID: Int,
    @SerializedName("LocationName") val locationName: String?,
    @SerializedName("LocationDescription") val locationDescription: String?,
    @SerializedName("LocationAddress") val locationAddress: String?,
    @SerializedName("LocationPostcode") val locationPostcode: String?,
    @SerializedName("LocationLatitude") val locationLatitude: Double,
    @SerializedName("LocationLongitude") val locationLongitude: Double
)
