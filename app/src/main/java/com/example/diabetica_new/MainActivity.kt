package com.example.diabetica_new

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diabetica.data.database.AppDatabase
import com.example.diabetica.data.repository.HealthRecordRepository
import com.example.diabetica.navigation.AppNavigation
import com.example.diabetica.ui.theme.DiabeticaTheme
import com.example.diabetica.utils.PreferencesManager
import com.example.diabetica.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация БД и репозитория
        val database = AppDatabase.getInstance(this)
        val repository = HealthRecordRepository(database.healthRecordDao())
        val preferencesManager = PreferencesManager(this)

        setContent {
            // Создаем ViewModel с фабрикой
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(repository, preferencesManager)
            )

            val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)

            DiabeticaTheme(
                darkTheme = isDarkMode
            ) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}

// Фабрика для ViewModel
class MainViewModelFactory(
    private val repository: HealthRecordRepository,
    private val preferencesManager: PreferencesManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}