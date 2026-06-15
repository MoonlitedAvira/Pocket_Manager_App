// ui/screens/SanTestScreen.kt
package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import ru.moonlited.pocketmanager.viewmodel.SanViewModel

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanTestScreen(
    viewModel: SanViewModel,
    fromWorkStart: Boolean = false,
    onOpenDrawer: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTimer: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    var scoreS by remember { mutableFloatStateOf(4f) }
    var scoreA by remember { mutableFloatStateOf(4f) }
    var scoreN by remember { mutableFloatStateOf(4f) }

    val isSaved by viewModel.isSaved.collectAsState()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !isSaved) {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оценка САН") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, "Информация")
                    }
                }
            )
        }
    ) { padding ->
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("О тесте САН") },
                text = { Text("Опросник САН (Самочувствие, Активность, Настроение) предназначен для оперативной оценки самочувствия, активности и настроения. Оцените свое состояние по 7-балльной шкале, где 1 — хуже всего, а 7 — лучше всего.") },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) { Text("Понятно") }
                }
            )
        }
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Вы уверены?") },
                text = { Text("Прогресс прохождения теста не будет сохранен. Вы действительно хотите выйти?") },
                confirmButton = {
                    TextButton(onClick = { 
                        showExitDialog = false
                        onExit()
                    }) { Text("Выйти") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text("Отмена") }
                }
            )
        }
        var showRecommendationDialog by remember { mutableStateOf(false) }

        if (showRecommendationDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showRecommendationDialog = false
                    viewModel.saveResults(scoreS, scoreA, scoreN)
                },
                title = { Text("Обратите внимание") },
                text = { Text("Ваши показатели ниже нормы (меньше 4). Рекомендуем сделать небольшой перерыв, выйти на свежий воздух или выпить воды. Забота о себе — приоритет.") },
                confirmButton = {
                    TextButton(onClick = { 
                        showRecommendationDialog = false
                        viewModel.saveResults(scoreS, scoreA, scoreN)
                    }) { Text("Понятно, сохранить результат") }
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSaved) {
                Spacer(modifier = Modifier.weight(1f))
                Text("Результаты успешно сохранены!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                if (fromWorkStart) {
                    Button(onClick = { onNavigateToTimer() }) { Text("Перейти к таймеру работы") }
                } else {
                    Button(onClick = { onNavigateToStats() }) { Text("Перейти к статистике") }
                }
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Text("Оцените состояние", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 32.dp))
                SanSlider(label = "Самочувствие", value = scoreS, onValueChange = { scoreS = it })
                Spacer(modifier = Modifier.height(24.dp))
                SanSlider(label = "Активность", value = scoreA, onValueChange = { scoreA = it })
                Spacer(modifier = Modifier.height(24.dp))
                SanSlider(label = "Настроение", value = scoreN, onValueChange = { scoreN = it })

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { 
                        if (scoreS < 4f || scoreA < 4f || scoreN < 4f) {
                            showRecommendationDialog = true
                        } else {
                            viewModel.saveResults(scoreS, scoreA, scoreN) 
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Сохранить результаты", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun SanSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = "${value.roundToInt()}/7", style = MaterialTheme.typography.bodyLarge)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 1f..7f, steps = 5)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Хуже", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Лучше", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}