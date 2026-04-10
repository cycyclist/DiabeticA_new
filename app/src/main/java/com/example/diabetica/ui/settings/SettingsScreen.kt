package com.example.diabetica.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diabetica.viewmodel.MainViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isRussian by viewModel.isRussianLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isRussian) "Настройки" else "Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text(if (isRussian) "← Назад" else "← Back")
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null)
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Text(if (isRussian) "Язык" else "Language")
                    }
                    Text(
                        text = if (isRussian) "Русский" else "English",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка смены языка
            Button(
                onClick = { viewModel.toggleLanguage() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRussian) "Сменить на английский" else "Switch to Russian")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка перехода к сенсору
            Button(
                onClick = { navController.navigate("juggluco") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRussian) "Данные сенсора" else "Sensor Data")
            }

            // После блока с языком, добавьте:

            Spacer(modifier = Modifier.height(16.dp))

            // Единицы измерения
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isRussian) "Единицы измерения" else "Units")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !viewModel.useMmol.value,
                            onClick = { if (viewModel.useMmol.value) viewModel.toggleGlucoseUnit() },
                            label = { Text("мг/дл") }
                        )
                        FilterChip(
                            selected = viewModel.useMmol.value,
                            onClick = { if (!viewModel.useMmol.value) viewModel.toggleGlucoseUnit() },
                            label = { Text("ммоль/л") }
                        )
                    }
                }
                Text(if (isRussian) "Данные сенсора Sibionics" else "Sibionics Sensor data")
            }
        }
    }
}