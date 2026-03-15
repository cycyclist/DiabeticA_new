package com.example.diabetica.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.data.repository.HealthRecordRepository
import com.example.diabetica.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: HealthRecordRepository,
    private val preferencesManager: PreferencesManager
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