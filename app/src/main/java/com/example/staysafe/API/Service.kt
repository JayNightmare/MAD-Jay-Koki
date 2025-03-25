package com.example.staysafe.API

import com.example.staysafe.model.data.*
import retrofit2.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface Service {
    // ! Activities
    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/activities")
    fun getAllActivities(): Call<List<Activity>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/activities/{activityID}")
    fun getActivity(@Path("activityID") id: Long): Call<List<Activity>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/activities/users/{activityUserID}")
    fun getUserActivities(@Path("activityUserID") activityUserID: Long): Call<List<Activity>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/positions/{positionActivityID}")
    fun getActivityPositions(@Path("positionActivityID") activityID: Long): Call<List<Position>>

    @POST("https://softwarehub.uk/unibase/staysafe/v1/api/activities")
    fun addActivities(@Body activity: Activity): Call<List<Activity>>

    @PUT("https://softwarehub.uk/unibase/staysafe/v1/api/activities/{activityID}")
    fun updateActivity(@Path("activityID") id: Long): Call<List<Activity>>

    @DELETE("https://softwarehub.uk/unibase/staysafe/v1/api/activities/{activityID}")
    fun deleteActivity(@Path("activityID") id: Long): Call<Unit>

    // //

    // ! Locations
    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/locations")
    fun getAllLocations(): Call<List<Location>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/locations/{locationID}")
    fun getLocation(@Path("locationID") id: Long): Call<List<Location>>

    //Just in case (If you do not want just delete it)
    @POST("https://softwarehub.uk/unibase/staysafe/v1/api/locations")
    fun addLocation(@Body location: Location): Call<List<Location>>

    @PUT("https://softwarehub.uk/unibase/staysafe/v1/api/locations/{locationID}")
    fun updateLocation(@Path("locationID") id: Long): Call<List<Location>>

    @DELETE("https://softwarehub.uk/unibase/staysafe/v1/api/locations/{locationID}")
    fun deleteLocation(@Path("locationID") id: Long): Call<Unit>

    // //

    // ! Positions
    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/positions")
    fun getAllPositions(): Call<List<Position>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/positions/{positionID}")
    fun getPositions(@Path("positionID") id: Long): Call<List<Position>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/positions/{positionActivityID}")
    fun getActivityPosition(@Path("positionActivityID") id: Long): Call<List<Position>>

    //Just in case (If you do not want just delete it)
    @POST("https://softwarehub.uk/unibase/staysafe/v1/api/positions")
    fun addPositions(@Body position: Position): Call<List<Position>>

    @PUT("https://softwarehub.uk/unibase/staysafe/v1/api/positions/{positionID}")
    fun updatePosition(@Path("positionID") id: Long): Call<List<Position>>

    @DELETE("https://softwarehub.uk/unibase/staysafe/v1/api/positions/{positionID}")
    fun deletePosition(@Path("positionID") id: Long): Call<Unit>

    // //

    // ! Status
    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/status")
    fun getStatus(): Call<List<Status>>

    // //

    // ! Users
    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/users")
    fun getUsers(): Call<List<User>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/users/{userID}")
    fun getUser(@Path("userID") id: Long): Call<List<User>>

    @GET("https://softwarehub.uk/unibase/staysafe/v1/api/users/contacts/{userID}")
    fun getUserContact(@Path("userID") userID: Long): Call<List<UserWithContact>>

    // ? Insert user
    @POST("https://softwarehub.uk/unibase/staysafe/v1/api/users")
    fun addUser(@Body user: User): Call<List<User>>

    // ? Update user
    @PUT("https://softwarehub.uk/unibase/staysafe/v1/api/users/{userID}")
    fun updateUser(@Path("userID") id: Long, @Body user: User): Call<List<User>>

    // ? Delete user
    @DELETE("https://softwarehub.uk/unibase/staysafe/v1/api/users/{userID}")
    fun deleteUser(@Path("userID") id: Long): Call<Unit>

    // //

    // ! Contact (Emergency contact)
    @POST("https://softwarehub.uk/unibase/staysafe/v1/api/contacts")
    fun addContact(@Body contact: Contact): Call<List<Contact>>

    @DELETE("https://softwarehub.uk/unibase/staysafe/v1/api/contacts/{contactID}")
    fun deleteContact(@Path("contactID") id: Long): Call<List<Unit>>

// //
}