package com.example.diabetica.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.viewmodel.MainViewModel

/**
 * Главный экран приложения
 * Отображает список записей и кнопку добавления новой записи
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    // Подписка на состояние (MVVM - View наблюдает за ViewModel)
    val records by viewModel.allRecords.collectAsState()
    val isRussian by viewModel.isRussianLanguage.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isRussian) "Мои показатели" else "My Records")
                },
                actions = {
                    // Индикатор интернет-соединения
                    IconButton(
                        onClick = { /* Можно добавить действие при клике */ }
                    ) {
                        Icon(
                            imageVector = if (isNetworkAvailable) Icons.Default.Done else Icons.Default.Close,
                            contentDescription = if (isNetworkAvailable) "Online" else "Offline",
                            tint = if (isNetworkAvailable) Color.Green else Color.Red
                        )
                    }

                    // Кнопка настроек
                    IconButton(
                        onClick = { navController.navigate("settings") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("detail/0") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        // Основной контент
        if (records.isEmpty()) {
            // Пустое состояние
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isRussian) "Нет записей" else "No records",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (isRussian)
                            "Нажмите + для добавления"
                        else
                            "Tap + to add",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // Список записей
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = records,
                    key = { record -> record.id }  // Уникальный ключ для каждого элемента
                ) { record ->
                    RecordCard(
                        record = record,
                        onClick = {
                            // При клике передаем ID записи
                            navController.navigate("detail/${record.id}")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Карточка записи
 */
@Composable
fun RecordCard(
    record: HealthRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Описание
            Text(
                text = record.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Дата
            Text(
                text = record.getFormattedDate(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}