package com.example.diabetica.data.juggluco

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class JugglucoGlucose(
    val timestamp: Long,
    val glucose: Double,        // в мг/дл или ммоль/л
    val trend: Int,             // 0-8 направление тренда
    val trendDescription: String? = null,
    val delta: Double? = null,   // изменение за последние 5 минут
    val battery: Int? = null     // заряд батареи сенсора
) {
    val formattedTime: String
        get() {
            val date = Date(timestamp)
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            return format.format(date)
        }

    val formattedDate: String
        get() {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return format.format(date)
        }

    val trendSymbol: String
        get() = when (trend) {
            0 -> "→"  // Стабильно
            1 -> "↗"  // Растет медленно
            2 -> "↑"  // Растет быстро
            3 -> "↘"  // Падает медленно
            4 -> "↓"  // Падает быстро
            5 -> "↗↗" // Растет очень быстро
            6 -> "↘↘" // Падает очень быстро
            7 -> "⇈"  // Растет экстремально
            8 -> "⇊"  // Падает экстремально
            else -> "?"
        }
}

data class JugglucoStatus(
    val version: String,
    val uptime: Long,
    val sensorConnected: Boolean,
    val sensorBattery: Int? = null,
    val sensorDaysLeft: Int? = null,
    val transmitterBattery: Int? = null
)

data class JugglucoSensorInfo(
    val sensorId: String,
    val sensorStarted: Long,
    val sensorExpires: Long,
    val sensorDaysTotal: Int,
    val sensorDaysLeft: Int,
    val transmitterId: String,
    val transmitterBattery: Int
)

data class JugglucoStats(
    val averageGlucose: Double,
    val minGlucose: Double,
    val maxGlucose: Double,
    val standardDeviation: Double,
    val timeInRange: TimeInRange,
    val readingsCount: Int
)

data class TimeInRange(
    val below70: Double,     // % времени ниже 70
    val inRange70_180: Double, // % времени в целевом диапазоне
    val above180: Double,    // % времени выше 180
    val above250: Double     // % времени выше 250
)