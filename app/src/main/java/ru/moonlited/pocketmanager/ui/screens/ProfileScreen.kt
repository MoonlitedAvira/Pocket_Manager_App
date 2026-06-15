package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.moonlited.pocketmanager.viewmodel.ProfileViewModel
import ru.moonlited.pocketmanager.viewmodel.PomodoroViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    pomodoroViewModel: PomodoroViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSan: () -> Unit,
    onNavigateToWorkingTimer: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = ru.moonlited.pocketmanager.utils.SessionManager(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", java.util.Locale.Builder().setLanguage("ru").build())
                    val dateString = today.format(formatter)
                    
                    Text(
                        text = "Добрый день!\n$dateString",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    user?.let { u ->
                        val roleTranslated = when (u.role) {
                            "director" -> "Директор"
                            "manager" -> "Менеджер"
                            "worker" -> "Сотрудник"
                            else -> "Самозанятый"
                        }
                        Text(
                            text = u.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (u.role != "self_employed") {
                            Text(
                                text = "${u.companyName ?: "Нет компании"} • $roleTranslated",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = roleTranslated,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    // Заглушки для статистики
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(title = "Отработано", value = "0 ч", modifier = Modifier.weight(1f))
                        StatCard(title = "Рабочих дней", value = "0 дн", modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                                Column {
                                    Text("Streak", style = MaterialTheme.typography.labelMedium)
                                    Text("0 дней", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                Column {
                                    Text("Пунктуальность", style = MaterialTheme.typography.labelMedium)
                                    Text("0%", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    val latestRecord = attendances.maxByOrNull { it.date }
                    val isWorking = latestRecord?.actionType == "check_in"
                    
                    var showSanDialog by remember { mutableStateOf(false) }

                    if (showSanDialog) {
                        AlertDialog(
                            onDismissRequest = { showSanDialog = false },
                            title = { Text("Начало работы") },
                            text = { Text("Желаете пройти тест САН для оценки вашего состояния перед работой?") },
                            confirmButton = {
                                TextButton(onClick = { 
                                    showSanDialog = false 
                                    onNavigateToSan()
                                }) { Text("Да") }
                            },
                            dismissButton = {
                                TextButton(onClick = { 
                                    showSanDialog = false 
                                    viewModel.checkIn("check_in")
                                    onNavigateToWorkingTimer()
                                }) { Text("Нет, начать") }
                            }
                        )
                    }

                    if (isWorking) {
                        Button(
                            onClick = { onNavigateToWorkingTimer() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Вернуться к таймеру работы", fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    } else {
                        Button(
                            onClick = { showSanDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Начать работу", fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }

                item {
                    Text("Последние рабочие дни", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (attendances.isEmpty()) {
                    item {
                        Text("Нет записей", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val grouped = attendances.groupBy { it.date.take(10) }.toList().sortedByDescending { it.first }.take(5)
                    items(grouped) { (dateStr, records) ->
                        val hasCheckIn = records.any { it.actionType == "check_in" }
                        val hasCheckOut = records.any { it.actionType == "check_out" }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dateStr, fontWeight = FontWeight.SemiBold)
                                if (hasCheckIn && hasCheckOut) {
                                    Text("Завершен", color = Color(0xFF4CAF50))
                                } else if (hasCheckIn) {
                                    Text("В процессе", color = Color(0xFFFFC107))
                                } else {
                                    Text("Неполный", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
