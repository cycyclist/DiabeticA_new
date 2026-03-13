package com.example.diabetica.data.repository

import com.example.diabetica.data.dao.HealthRecordDao
import com.example.diabetica.data.entity.HealthRecord
import kotlinx.coroutines.flow.Flow

class HealthRecordRepository(
    private val dao: HealthRecordDao
) {
    fun getAllRecords(): Flow<List<HealthRecord>> = dao.getAllRecords()

    suspend fun insert(record: HealthRecord) = dao.insert(record)

    suspend fun update(record: HealthRecord) = dao.update(record)

    suspend fun delete(record: HealthRecord) = dao.delete(record)
}