package com.example.diabetica.ui.juggluco

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diabetica.data.juggluco.JugglucoGlucose
import com.example.diabetica.data.repository.JugglucoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugglucoScreen(
    repository: JugglucoRepository,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val isConnected by repository.isConnected.collectAsState()
    val latestGlucose by repository.latestGlucose.collectAsState()

    var history by remember { mutableStateOf<List<JugglucoGlucose>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var sensorStatus by remember { mutableStateOf("") }

    // Загружаем историю при запуске
    LaunchedEffect(Unit) {
        isLoading = true
        history = repository.getGlucoseHistory(6)
        //sensorStatus = repository.getSensorStatus()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Данные сенсора Sibionics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Индикатор подключения
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        if (isConnected) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = "Connected",
                                tint = Color.Green
                            )
                        } else {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Disconnected",
                                tint = Color.Red
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        repository.refresh()
                        history = repository.getGlucoseHistory(6)
                        //sensorStatus = repository.getSensorStatus()
                        isLoading = false
                    }
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Обновить")
            }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Карточка статуса подключения
                item {
                    ConnectionStatusCard(
                        isConnected = isConnected,
                        sensorStatus = sensorStatus
                    )
                }

                // Последнее показание
                if (latestGlucose != null) {
                    item {
                        LatestGlucoseCard(latestGlucose!!)
                    }
                }

                // Кнопка синхронизации истории
                item {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                repository.syncHistory(24)
                                history = repository.getGlucoseHistory(24)
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountBox, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Синхронизировать историю (24ч)")
                    }
                }

                // История показаний
                if (history.isNotEmpty()) {
                    item {
                        Text(
                            text = "История показаний",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(history) { glucose ->
                        GlucoseHistoryItem(glucose)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    sensorStatus: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isConnected) "✅ Подключено к Juggluco" else "❌ Нет подключения",
                    style = MaterialTheme.typography.titleMedium
                )

                if (!isConnected) {
                    Text(
                        text = "Проверьте: порт 17580/17581",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (sensorStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sensorStatus,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LatestGlucoseCard(glucose: JugglucoGlucose) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Текущий уровень глюкозы",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${glucose.glucose} мг/дл",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = glucose.trendSymbol,
                    style = MaterialTheme.typography.headlineMedium,
                    color = when (glucose.trend) {
                        0 -> Color.Gray
                        in 1..3 -> Color.Green
                        in 4..6 -> Color.Red
                        else -> Color.Magenta
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Измерено: ${glucose.formattedDate}",
                style = MaterialTheme.typography.bodySmall
            )

            glucose.delta?.let {
                Text(
                    text = "Изменение: ${if (it > 0) "+" else ""}$it мг/дл за 5 мин",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun GlucoseHistoryItem(glucose: JugglucoGlucose) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = glucose.formattedTime,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${glucose.glucose} мг/дл",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = glucose.trendSymbol,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}