package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.staysafe.model.data.Position

@Dao
interface PositionDao {
    @Insert
    suspend fun insertPosition(position: Position): Long

    @Query("SELECT * FROM positions WHERE positionActivityID = :activityID")
    fun getPositionsForActivity(activityID: Long): List<Position>

    @Delete
    suspend fun deletePosition(position: Position)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dummyPositions: List<Position>)
}
