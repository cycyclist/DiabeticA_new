package com.example.diabetica.data.dao

import androidx.room.*
import com.example.diabetica.data.entity.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Insert
    suspend fun insert(record: HealthRecord)

    @Update
    suspend fun update(record: HealthRecord)

    @Delete
    suspend fun delete(record: HealthRecord)

    @Query("SELECT * FROM health_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<HealthRecord>>
}