package com.example.diabetica

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diabetica.data.database.AppDatabase
import com.example.diabetica.data.juggluco.JugglucoGlucose
import com.example.diabetica.data.repository.HealthRecordRepository
import com.example.diabetica.data.repository.SimpleJugglucoRepository
import com.example.diabetica.navigation.AppNavigation
import com.example.diabetica.ui.theme.DiabeticaTheme
import com.example.diabetica.utils.PreferencesManager
import com.example.diabetica.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var jugglucoRepository: SimpleJugglucoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация БД и репозитория
        val database = AppDatabase.getInstance(this)
        val repository = HealthRecordRepository(database.healthRecordDao())
        val preferencesManager = PreferencesManager(this)

        // Инициализация репозитория сенсора
        jugglucoRepository = SimpleJugglucoRepository(this, database)

        // Проверяем подключение к Juggluco
        lifecycleScope.launch {
            jugglucoRepository.initialize()
        }

        setContent {
            // Создаем ViewModel с фабрикой
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(repository, preferencesManager, applicationContext)
            )

            val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)

            DiabeticaTheme(
                darkTheme = isDarkMode
            ) {
                AppNavigation(
                    viewModel = viewModel,
                    jugglucoRepository = jugglucoRepository
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::jugglucoRepository.isInitialized) {
            jugglucoRepository.disconnect()
        }
    }
}

// Фабрика для ViewModel
class MainViewModelFactory(
    private val repository: HealthRecordRepository,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, preferencesManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}