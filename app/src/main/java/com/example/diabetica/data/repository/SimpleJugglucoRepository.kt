package com.example.diabetica.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.diabetica.data.database.AppDatabase
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.data.juggluco.GlucoseData
import com.example.diabetica.data.juggluco.SimpleJugglucoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SimpleJugglucoRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val client = SimpleJugglucoClient()
    private val healthRecordDao = database.healthRecordDao()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _latestGlucose = MutableStateFlow<GlucoseData?>(null)
    val latestGlucose: StateFlow<GlucoseData?> = _latestGlucose.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val available = client.isAvailable()
            _isConnected.value = available

            if (available) {
                val glucose = client.getLatestGlucose()
                if (glucose != null) {
                    _latestGlucose.value = glucose
                    saveToDatabase(glucose)
                }
                startPolling()
            }
            available
        } catch (e: Exception) {
            _error.value = "Ошибка подключения: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun startPolling() {
        while (_isConnected.value) {
            delay(60_000)
            refresh()
        }
    }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        try {
            val glucose = client.getLatestGlucose()
            if (glucose != null) {
                _latestGlucose.value = glucose
                saveToDatabase(glucose)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            _error.value = "Ошибка обновления: ${e.message}"
            false
        }
    }

    suspend fun getHistory(hours: Int): List<GlucoseData> = withContext(Dispatchers.IO) {
        try {
            client.getGlucoseHistory(hours)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun saveToDatabase(glucose: GlucoseData) {
        val record = HealthRecord(
            title = "Глюкоза (сенсор)",
            description = "Уровень: ${glucose.value} мг/дл ${glucose.trendSymbol}\nИзменение: ${glucose.delta}",
            timestamp = glucose.timestamp,
            glucoseLevel = glucose.value
        )
        healthRecordDao.insert(record)
    }

    /**
     * Получить график глюкозы
     */
    suspend fun getGlucoseCurve(
        hours: Int = 6,
        width: Int = 800,
        height: Int = 400,
        darkMode: Boolean = false,
        useMmol: Boolean = false
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            client.getGlucoseCurve(hours, width, height, darkMode, useMmol)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSensorStatus(): String = withContext(Dispatchers.IO) {
        try {
            val info = client.getSensorInfo()
            if (info != null) {
                buildString {
                    appendLine("Сенсор: подключен")
                    appendLine("Дней работы: ${String.format("%.1f", info.sensorAgeDays)}")
                    appendLine("Осталось дней: ${info.sensorDaysLeft}")
                    appendLine("Батарея: ${info.transmitterBattery}%")
                    appendLine("Единицы: ${info.unit}")
                }
            } else {
                "Сенсор: данные недоступны"
            }
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    fun disconnect() {
        _isConnected.value = false
    }
}