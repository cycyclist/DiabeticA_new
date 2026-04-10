package com.example.diabetica.ui.juggluco

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.diabetica.data.juggluco.GlucoseData
import com.example.diabetica.data.repository.SimpleJugglucoRepository
import com.example.diabetica.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleJugglucoScreen(
    repository: SimpleJugglucoRepository,
    navController: NavController,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    val isConnected by repository.isConnected.collectAsState()
    val latestGlucose by repository.latestGlucose.collectAsState()
    val isLoading by repository.isLoading.collectAsState()
    val error by repository.error.collectAsState()

    val useMmol by viewModel.useMmol.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Состояния для UI
    var history by remember { mutableStateOf<List<GlucoseData>>(emptyList()) }
    var sensorStatus by remember { mutableStateOf("") }
    var curveBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoadingCurve by remember { mutableStateOf(false) }
    var selectedHours by remember { mutableStateOf(6) }

    // ЗАГРУЗКА ТОЛЬКО ПРИ ПЕРВОМ ОТКРЫТИИ
    LaunchedEffect(Unit) {
        // Загружаем историю
        history = repository.getHistory(selectedHours)
        sensorStatus = repository.getSensorStatus()
    }

    // Загрузка графика при изменении периода или темы
    LaunchedEffect(selectedHours, useMmol, isDarkMode) {
        isLoadingCurve = true
        curveBitmap = repository.getGlucoseCurve(
            hours = selectedHours,
            width = 800,
            height = 400,
            darkMode = isDarkMode,
            useMmol = useMmol
        )
        isLoadingCurve = false
    }

    fun formatValue(value: Double): String {
        return if (useMmol) {
            String.format("%.1f", value / 18.018)
        } else {
            String.format("%.0f", value)
        }
    }

    fun getUnitSuffix(): String {
        return if (useMmol) "ммоль/л" else "мг/дл"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сенсор Sibionics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            // Ручное обновление
                            repository.refresh()
                            history = repository.getHistory(selectedHours)
                            sensorStatus = repository.getSensorStatus()
                            isLoadingCurve = true
                            curveBitmap = repository.getGlucoseCurve(
                                hours = selectedHours,
                                width = 800,
                                height = 400,
                                darkMode = isDarkMode,
                                useMmol = useMmol
                            )
                            isLoadingCurve = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { paddingValues ->
        // ВАЖНО: Используем if(!isLoading) а не else, чтобы избежать проблем
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Подключение к сенсору...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Статус подключения
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isConnected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isConnected) "✅ Подключено к Juggluco" else "❌ Нет подключения",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (!isConnected) {
                                Text("IP: 192.168.0.245:17580")
                            }
                        }
                    }
                }

                // Выбор периода
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Период графика", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val periods = listOf(3 to "3ч", 6 to "6ч", 12 to "12ч", 24 to "24ч")
                                periods.forEach { (hours, label) ->
                                    FilterChip(
                                        selected = selectedHours == hours,
                                        onClick = { selectedHours = hours },
                                        label = { Text(label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // График
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("График глюкозы", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLoadingCurve) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (curveBitmap != null) {
                                Image(
                                    bitmap = curveBitmap!!.asImageBitmap(),
                                    contentDescription = "График глюкозы",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentScale = ContentScale.FillWidth
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Не удалось загрузить график")
                                }
                            }
                        }
                    }
                }

                // Последнее показание
                if (latestGlucose != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Текущий уровень", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${formatValue(latestGlucose!!.value)} ${getUnitSuffix()}",
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                    Text(
                                        text = latestGlucose!!.trendSymbol,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = when (latestGlucose!!.direction) {
                                            "DoubleUp", "SingleUp", "FortyFiveUp" -> Color.Red
                                            "DoubleDown", "SingleDown", "FortyFiveDown" -> Color.Green
                                            else -> Color.Gray
                                        }
                                    )
                                }
                                Text("Изменение: ${formatValue(latestGlucose!!.delta)} ${getUnitSuffix()}",
                                    style = MaterialTheme.typography.bodySmall)
                                Text(latestGlucose!!.formattedDate, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Статус сенсора
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Информация о сенсоре", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(sensorStatus, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // История
                if (history.isNotEmpty()) {
                    item {
                        Text(
                            text = "История (${selectedHours} часов)",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(history) { glucose ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(glucose.formattedTime, style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        "${formatValue(glucose.value)} ${getUnitSuffix()}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(glucose.trendSymbol, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }

                // Ошибка
                if (error != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = error!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}