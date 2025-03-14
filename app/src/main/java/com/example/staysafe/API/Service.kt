package com.example.staysafe.API

import com.example.staysafe.model.data.Activity
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.Position
import com.example.staysafe.model.data.Status
import com.example.staysafe.model.data.User
import kotlinx.coroutines.flow.Flow
import retrofit2.*
import retrofit2.http.GET
import retrofit2.http.Path

interface Service {
    //Activities
    @GET("http://softwarehub.uk/unibase/staysafe/v1/api/activities")
    fun getAllActivities(): Call<List<Activity>>
    @GET("http://softwarehub.uk/unibase/staysafe/v1/api/activities/{activityID}")
    fun getActivity(@Path("activityID") id:Long): Call<List<Activity>>
    @GET("http://softwarehub.uk/unibase/staysafe/v1/api/activities/users/{userID}")
    fun getActivityUser(@Path("userID") id:Long): Call<List<Activity>>

    //Locations
    @GET("http://softwarehub.uk/unibase/staysafe/v1/api/locations")
    fun getAllLocations():Call<List<Location>>
    @GET("http://softwarehub.uk/unibase/staysafe/v1/api/locations/{locationID}")
    fun getLocation(@Path("locationID") id:Long): Call<List<Location>>

    //Positions
    @GET("http://softwarehub.uk/unibase/staysafe/v1/api/positions")
    fun getAllPositions(): Call<List<Position>>
    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/positions/{positionID}")
    fun getPositions(@Path("positionID") id:Long): Call<List<Position>>
    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/positions/{positionActivityID}")
    fun getActivityPosition(@Path("positionActivityID") id: Long): Call<List<Position>>

    //Status
    @GET("http://softwarehub.uk/unibase/staysafe/v1/api/status")
    fun getStatus(): Call<List<Status>>

    //Users
    @GET("users")
    fun getUsers(): Call<List<User>>
    @GET("users/{userID}")
    fun getUser(@Path("userID") id: Long): Call<List<User>>
    @GET("users/contacts/{userID}")
    fun getUserContact(@Path("userID") id: Long): Call<List<User>>


}