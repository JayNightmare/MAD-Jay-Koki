package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.staysafe.model.data.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE userID = :id")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>  // Ensure this matches the entity
}
