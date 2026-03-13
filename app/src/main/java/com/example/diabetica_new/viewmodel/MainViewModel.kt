package com.example.diabetica.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.data.repository.HealthRecordRepository
import com.example.diabetica.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: HealthRecordRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Для списка записей
    val allRecords = repository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Для темы
    val isDarkMode = preferencesManager.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Для языка
    val isRussianLanguage = preferencesManager.isRussianLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleTheme() {
        viewModelScope.launch {
            val current = isDarkMode.value
            preferencesManager.setDarkMode(!current)
        }
    }

    fun getRecord(id: Int): Flow<HealthRecord?> {
        return flow {
            // В реальном проекте здесь должен быть запрос к репозиторию
            // Пока возвращаем null
            emit(null)
        }.flowOn(Dispatchers.IO)
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
                description = description
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