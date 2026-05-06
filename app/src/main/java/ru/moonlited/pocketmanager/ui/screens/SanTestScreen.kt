// ui/screens/SanTestScreen.kt
package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import ru.moonlited.pocketmanager.viewmodel.SanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanTestScreen(viewModel: SanViewModel, onOpenDrawer: () -> Unit) {
    var scoreS by remember { mutableFloatStateOf(4f) }
    var scoreA by remember { mutableFloatStateOf(4f) }
    var scoreN by remember { mutableFloatStateOf(4f) }

    val isSaved by viewModel.isSaved.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оценка САН") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSaved) {
                Spacer(modifier = Modifier.weight(1f))
                Text("Результаты успешно сохранены!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.resetState() }) { Text("Пройти заново") }
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
                    onClick = { viewModel.saveResults(scoreS, scoreA, scoreN) },
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