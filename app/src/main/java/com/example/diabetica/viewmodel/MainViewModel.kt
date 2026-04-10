package com.example.diabetica.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.data.repository.HealthRecordRepository
import com.example.diabetica.utils.NetworkUtils
import com.example.diabetica.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: HealthRecordRepository,
    private val preferencesManager: PreferencesManager,
    context: Context
) : ViewModel() {

    val allRecords = repository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isDarkMode = preferencesManager.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val useMmol = preferencesManager.useMmol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleGlucoseUnit() {
        viewModelScope.launch {
            val current = useMmol.value
            preferencesManager.setGlucoseUnit(!current)
        }
    }

    // Состояние интернет-соединения
    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    init {
        // Наблюдаем за изменениями сети
        viewModelScope.launch {
            NetworkUtils.observeNetworkConnectivity(context).collect { isConnected ->
                _isNetworkAvailable.value = isConnected

                // Если интернет появился, можно обновить данные сенсора
                if (isConnected) {
                    // Обновление данных при появлении интернета
                    refreshDataIfNeeded()
                }
            }
        }
    }

    private fun refreshDataIfNeeded() {
        viewModelScope.launch {
            // Здесь можно обновить данные, требующие интернета
            // Например, новости или синхронизацию с сенсором
        }
    }
    // Для языка
    val isRussianLanguage = preferencesManager.isRussianLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // ДОБАВЬТЕ ЭТОТ МЕТОД
    suspend fun getRecord(id: Int): HealthRecord? {
        return repository.getRecordById(id)
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = isDarkMode.value
            preferencesManager.setDarkMode(!current)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = isRussianLanguage.value
            preferencesManager.setLanguage(!current)
        }
    }

    fun addRecord(title: String, description: String) {
        viewModelScope.launch {
            val record = HealthRecord(
                title = title,
                description = description,
                glucoseLevel = null
            )
            repository.insert(record)
        }
    }

    fun updateRecord(record: HealthRecord) {
        viewModelScope.launch {
            repository.update(record)
        }
    }

    fun deleteRecord(record: HealthRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }
}