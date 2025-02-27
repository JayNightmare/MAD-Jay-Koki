package com.example.staysafe.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.staysafe.model.data.Contact

@Dao
interface ContactDao {
    @Insert
    suspend fun insertContact(contact: Contact): Long

    @Query("SELECT * FROM contacts WHERE contactUserID = :userID")
    fun getContactsForUser(userID: Long): List<Contact>

    @Delete
    suspend fun deleteContact(contact: Contact)
}
