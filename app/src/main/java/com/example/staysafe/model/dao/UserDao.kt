package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.staysafe.model.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    //Create
    @Insert
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dummyUsers: List<User>)

    @Query("SELECT * FROM users WHERE userID = :id")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

//    @Update
//    suspend fun updateUser(user: User)
//
//    @Query("DELETE FROM users WHERE userID = :id")
//    suspend fun deleteUserById(id: Long)
//
//    @Query("SELECT COUNT(*) FROM users WHERE userID = :id")
//    suspend fun isUserExist(id: Long)
}
