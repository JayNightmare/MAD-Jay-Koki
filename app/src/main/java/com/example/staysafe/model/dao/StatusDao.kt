package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.staysafe.model.data.Status

@Dao
interface StatusDao {
    @Insert
    suspend fun insertStatus(status: Status): Long

    @Query("SELECT * FROM statuses WHERE statusID = :id")
    fun getStatusById(id: Long): Status?

    @Query("SELECT * FROM statuses ORDER BY statusOrder ASC")
    fun getAllStatuses(): List<Status>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dummyStatuses: List<Status>)
}
