package com.example.diabetica.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.diabetica.data.entity.HealthRecord
import com.example.diabetica.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: MainViewModel,
    navController: NavController,
    recordId: Int
) {
    val scope = rememberCoroutineScope()
    val isRussian by viewModel.isRussianLanguage.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var existingRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var isLoading by remember { mutableStateOf(recordId > 0) }

    // ЗАГРУЖАЕМ ЗАПИСЬ, ЕСЛИ ЭТО РЕДАКТИРОВАНИЕ
    LaunchedEffect(recordId) {
        if (recordId > 0) {
            isLoading = true
            // Получаем запись из репозитория
            val record = viewModel.getRecord(recordId)
            if (record != null) {
                title = record.title
                description = record.description
                existingRecord = record
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (recordId == 0)
                            if (isRussian) "Новая запись" else "New Record"
                        else
                            if (isRussian) "Редактирование записи" else "Edit Record"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Поле для заголовка
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isRussian) "Заголовок" else "Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле для описания
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isRussian) "Описание" else "Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = description.isBlank()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Кнопка сохранения
                    Button(
                        onClick = {
                            scope.launch {
                                if (existingRecord != null) {
                                    // Обновляем существующую запись
                                    val updatedRecord = existingRecord!!.copy(
                                        title = title,
                                        description = description
                                    )
                                    viewModel.updateRecord(updatedRecord)
                                } else {
                                    // Создаем новую запись
                                    viewModel.addRecord(title, description)
                                }
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank() && description.isNotBlank()
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRussian) "Сохранить" else "Save")
                    }

                    // Кнопка удаления (только для существующих записей)
                    if (existingRecord != null) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    viewModel.deleteRecord(existingRecord!!)
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRussian) "Удалить" else "Delete")
                        }
                    }
                }

                // Подсказка для пользователя
                if (title.isBlank() || description.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isRussian)
                            "Заполните все поля"
                        else
                            "Fill all fields",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}