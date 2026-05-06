// ui/screens/StatsScreen.kt
package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.moonlited.pocketmanager.data.api.SanTestResponse
import ru.moonlited.pocketmanager.viewmodel.SanViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: SanViewModel, onOpenDrawer: () -> Unit) {
    val history by viewModel.sanHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
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
                item {
                    Text("История тестов САН", style = MaterialTheme.typography.titleMedium)
                }

                items(history) { result ->
                    SanResultCard(result)
                }
            }
        }
    }
}

@Composable
fun SanResultCard(result: SanTestResponse) {
    val date = remember(result.date) {
        try {
            val parsed = ZonedDateTime.parse(result.date)
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}