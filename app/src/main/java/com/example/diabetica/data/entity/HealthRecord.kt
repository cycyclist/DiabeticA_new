package com.example.diabetica.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val glucoseLevel: Double? = null  // Добавлено!
) {
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
}