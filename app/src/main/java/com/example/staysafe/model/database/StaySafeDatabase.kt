package com.example.staysafe.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.staysafe.model.converters.Converters
// Import All Data Classes
import com.example.staysafe.model.data.*
// Import All DAO's Files
import com.example.staysafe.model.dao.*

@Database(
    entities = [User::class, Contact::class, Activity::class, Location::class, Status::class, Position::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StaySafeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun activityDao(): ActivityDao
    abstract fun contactDao(): ContactDao
    abstract fun locationDao(): LocationDao
    abstract fun statusDao(): StatusDao
    abstract fun positionDao(): PositionDao

    companion object {
        @Volatile private var INSTANCE: StaySafeDatabase? = null

        fun getDatabase(context: Context): StaySafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StaySafeDatabase::class.java,
                    "staySafe_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
