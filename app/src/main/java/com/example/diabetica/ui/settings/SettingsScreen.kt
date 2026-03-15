package com.example.diabetica.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diabetica.viewmodel.MainViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val isRussian by viewModel.isRussianLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isRussian) "Настройки" else "Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Тема
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Email, contentDescription = null)
                        Text(if (isRussian) "Темная тема" else "Dark Theme")
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleTheme() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Язык
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Text(if (isRussian) "Язык" else "Language")
                    }
                    Text(
                        text = if (isRussian) "Русский" else "English",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = { viewModel.toggleLanguage() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(if (isRussian) "Сменить язык на Английский" else "Switch to Russian")
            }

            Button(
                onClick = { navController.navigate("juggluco") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRussian) "Данные сенсора Sibionics" else "Sibionics Sensor data")
            }
        }
    }
}