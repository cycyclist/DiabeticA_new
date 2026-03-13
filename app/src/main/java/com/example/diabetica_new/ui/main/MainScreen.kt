package com.example.diabetica.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Функция для форматирования даты (добавьте её в начало файла)
fun HealthRecord.getFormattedDate(): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val records by viewModel.allRecords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val isRussian by viewModel.isRussianLanguage.collectAsState()
                    Text(if (isRussian) "Мои показатели" else "My Records")
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                val isRussian by viewModel.isRussianLanguage.collectAsState()
                Text(
                    text = if (isRussian) "Нет записей" else "No records",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    RecordCard(
                        record = record,
                        onClick = { navController.navigate("detail/${record.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordCard(
    record: HealthRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = record.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = record.getFormattedDate(),  // Здесь используется функция
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}