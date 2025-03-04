package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.staysafe.model.data.Location

@Dao
interface LocationDao  {
    @Insert
    suspend fun insertLocation(location: Location): Long

    @Query("SELECT * FROM locations WHERE locationID = :id")
    fun getLocationById(id: Long): Location?

    @Query("SELECT * FROM locations")
    fun getAllLocations(): List<Location>

    @Delete
    suspend fun deleteLocation(location: Location)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dummyLocations: List<Location>)
}
