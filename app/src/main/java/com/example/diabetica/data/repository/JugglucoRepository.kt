package com.example.diabetica.data.repository

import android.content.Context
import com.example.diabetica.data.database.AppDatabase
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.data.juggluco.JugglucoClient
import com.example.diabetica.data.juggluco.JugglucoGlucose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JugglucoRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val healthRecordDao = database.healthRecordDao()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _latestGlucose = MutableStateFlow<JugglucoGlucose?>(null)
    val latestGlucose: StateFlow<JugglucoGlucose?> = _latestGlucose.asStateFlow()

    private var jugglucoClient: JugglucoClient? = null

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            jugglucoClient = JugglucoClient.createAuto(context)
            val connected = jugglucoClient?.isAvailable() == true
            _isConnected.value = connected

            if (connected) {
                startObserving()
            }

            connected
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun startObserving() {
        CoroutineScope(Dispatchers.IO).launch {
            jugglucoClient?.observeGlucose()?.collect { glucose ->
                _latestGlucose.value = glucose

                val record = HealthRecord(
                    title = "Глюкоза (Juggluco)",
                    description = "Уровень: ${glucose.glucose} мг/дл ${glucose.trendSymbol}",
                    timestamp = glucose.timestamp,
                    glucoseLevel = glucose.glucose
                )
                healthRecordDao.insert(record)
            }
        }
    }

    suspend fun getGlucoseHistory(hours: Int): List<JugglucoGlucose> = withContext(Dispatchers.IO) {
        return@withContext jugglucoClient?.getGlucoseHistory(hours) ?: emptyList()
    }

    suspend fun syncHistory(hours: Int = 24): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = jugglucoClient ?: return@withContext false
            val history = client.getGlucoseHistory(hours)

            history.forEach { glucose ->
                val record = HealthRecord(
                    title = "Глюкоза (история)",
                    description = "Уровень: ${glucose.glucose} мг/дл",
                    timestamp = glucose.timestamp,
                    glucoseLevel = glucose.glucose
                )
                healthRecordDao.insert(record)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = jugglucoClient ?: return@withContext false
            val latest = client.getLatestGlucose()
            if (latest != null) {
                _latestGlucose.value = latest

                val record = HealthRecord(
                    title = "Глюкоза (ручное обновление)",
                    description = "Уровень: ${latest.glucose} мг/дл",
                    timestamp = latest.timestamp,
                    glucoseLevel = latest.glucose
                )
                healthRecordDao.insert(record)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        jugglucoClient = null
        _isConnected.value = false
    }
}