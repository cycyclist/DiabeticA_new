package com.example.diabetica.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (recordId == 0)
                            if (isRussian) "Новая запись" else "New Record"
                        else
                            if (isRussian) "Редактирование" else "Edit Record"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text(if (isRussian) "Назад" else "Back")
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (isRussian) "Заголовок" else "Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(if (isRussian) "Описание" else "Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            if (existingRecord != null) {
                                viewModel.updateRecord(
                                    existingRecord!!.copy(
                                        title = title,
                                        description = description
                                    )
                                )
                            } else {
                                viewModel.addRecord(title, description)
                            }
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRussian) "Сохранить" else "Save")
                }

                if (existingRecord != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                viewModel.deleteRecord(existingRecord!!)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRussian) "Удалить" else "Delete")
                    }
                }
            }
        }
    }
}