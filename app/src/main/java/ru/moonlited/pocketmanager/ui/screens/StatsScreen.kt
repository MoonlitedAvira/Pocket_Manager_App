// ui/screens/StatsScreen.kt
package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.moonlited.pocketmanager.data.api.SanTestResponse
import ru.moonlited.pocketmanager.data.api.MaslachResponse
import ru.moonlited.pocketmanager.data.api.MunsterbergResponse
import ru.moonlited.pocketmanager.viewmodel.SanViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: SanViewModel, onOpenDrawer: () -> Unit) {
    val history by viewModel.sanHistory.collectAsState()
    val maslachHistory by viewModel.maslachHistory.collectAsState()
    val munsterbergHistory by viewModel.munsterbergHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    var infoDialogTitle by remember { mutableStateOf<String?>(null) }
    var infoDialogText by remember { mutableStateOf<String?>(null) }

    if (infoDialogTitle != null && infoDialogText != null) {
        AlertDialog(
            onDismissRequest = { infoDialogTitle = null; infoDialogText = null },
            title = { Text(infoDialogTitle!!) },
            text = { Text(infoDialogText!!) },
            confirmButton = {
                TextButton(onClick = { infoDialogTitle = null; infoDialogText = null }) {
                    Text("Понятно")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                }
            )
        }
    ) { padding ->
        if (isLoading && history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (history.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("История тестов САН", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = {
                                infoDialogTitle = "САН"
                                infoDialogText = "С - Самочувствие\nА - Активность\nН - Настроение"
                            }) { Icon(Icons.Default.Info, contentDescription = "Инфо") }
                        }
                    }
                    items(history) { result -> SanResultCard(result) }
                }

                if (maslachHistory.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("История тестов Маслач", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = {
                                infoDialogTitle = "Тест Маслач"
                                infoDialogText = "ЭИ - Эмоциональное истощение\nДП - Деперсонализация\nПД - Профессиональные достижения"
                            }) { Icon(Icons.Default.Info, contentDescription = "Инфо") }
                        }
                    }
                    items(maslachHistory) { result -> MaslachResultCard(result) }
                }

                if (munsterbergHistory.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("История тестов Мюнстерберга", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = {
                                infoDialogTitle = "Тест Мюнстерберга"
                                infoDialogText = "Отображает количество правильно найденных слов среди случайных букв и время, затраченное на прохождение теста."
                            }) { Icon(Icons.Default.Info, contentDescription = "Инфо") }
                        }
                    }
                    items(munsterbergHistory) { result -> MunsterbergResultCard(result) }
                }
            }
        }
    }
}

@Composable
fun SanResultCard(result: SanTestResponse) {
    val date = remember(result.date) {
        try {
            val parsed = LocalDateTime.parse(result.date)
            parsed.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (_: Exception) { result.date }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("С", result.scoreS)
                StatItem("А", result.scoreA)
                StatItem("Н", result.scoreN)
            }
        }
    }
}

@Composable
fun RowScope.StatItem(label: String, value: Float) {
    val displayValue = kotlin.math.round(value).toInt().toString()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = displayValue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Composable
fun MaslachResultCard(result: MaslachResponse) {
    val date = remember(result.date) {
        try {
            val parsed = LocalDateTime.parse(result.date)
            parsed.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (_: Exception) { result.date }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("ЭИ", result.emotionalExhaustion)
                StatItem("ДП", result.depersonalization)
                StatItem("ПД", result.personalAccomplishment)
            }
        }
    }
}

@Composable
fun MunsterbergResultCard(result: MunsterbergResponse) {
    val date = remember(result.date) {
        try {
            val parsed = LocalDateTime.parse(result.date)
            parsed.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (_: Exception) { result.date }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Слов найдено", result.correctWords.toFloat())
                StatItem("Время (с)", result.timeSpentSeconds.toFloat())
            }
        }
    }
}