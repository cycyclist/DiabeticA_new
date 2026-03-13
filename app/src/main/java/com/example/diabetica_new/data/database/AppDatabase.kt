package com.example.diabetica.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.diabetica.data.dao.HealthRecordDao
import com.example.diabetica.data.entity.HealthRecord

@Database(entities = [HealthRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthRecordDao(): HealthRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}