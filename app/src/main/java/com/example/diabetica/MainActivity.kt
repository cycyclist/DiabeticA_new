package com.example.diabetica  // ВАЖНО: без _new!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diabetica.data.database.AppDatabase
import com.example.diabetica.data.repository.HealthRecordRepository
import com.example.diabetica.data.repository.JugglucoRepository
import com.example.diabetica.navigation.AppNavigation
import com.example.diabetica.ui.theme.DiabeticaTheme
import com.example.diabetica.utils.PreferencesManager
import com.example.diabetica.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var jugglucoRepository: JugglucoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(this)
        val repository = HealthRecordRepository(database.healthRecordDao())
        val preferencesManager = PreferencesManager(this)

        jugglucoRepository = JugglucoRepository(this, database)

        lifecycleScope.launch {
            jugglucoRepository.initialize()
        }

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(repository, preferencesManager)
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
        jugglucoRepository.disconnect()
    }
}

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