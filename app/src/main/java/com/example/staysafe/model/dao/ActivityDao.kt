package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.staysafe.model.data.Activity

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertActivity(activity: Activity): Long

    @Query("SELECT * FROM activities WHERE activityUserID = :userID")
    fun getUserActivities(userID: Long): List<Activity>

    @Query("SELECT * FROM activities WHERE activityID = :id")
    fun getActivityById(id: Long): Activity?

    @Delete
    suspend fun deleteActivity(activity: Activity)
}
