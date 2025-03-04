package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.staysafe.model.data.User

@Dao
interface UserDao {
    //Create
    @Insert
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dummyUsers: List<User>)
    //Get user
    @Query("SELECT * FROM users WHERE userID = :id")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>  // Ensure this matches the entity
    /*update*/
    @Update
    suspend fun updateUser(user: User)
    /*delete*/
    @Query("DELETE FROM users WHERE userID = :id")
    suspend fun deleteUserById(id: Long)
    /*Searching user that attached to the location*/
    @Query("SELECT COUNT(*) FROM users WHERE userID = :id")
    suspend fun isUserExist(id: Long)

}
