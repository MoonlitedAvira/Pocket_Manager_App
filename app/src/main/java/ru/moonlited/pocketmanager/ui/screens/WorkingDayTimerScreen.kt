package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.moonlited.pocketmanager.viewmodel.ProfileViewModel
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingDayTimerScreen(
    viewModel: ProfileViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToPomodoro: () -> Unit
) {
    val attendances by viewModel.attendances.collectAsState()
    val position by viewModel.position.collectAsState()

    val latestCheckIn = attendances.filter { it.actionType == "check_in" }.maxByOrNull { it.date }
    val latestCheckOut = attendances.filter { it.actionType == "check_out" }.maxByOrNull { it.date }
    
    val checkInInstant = latestCheckIn?.date?.let { 
        val dateStr = if (!it.endsWith("Z") && !it.contains("+")) it + "+03:00" else it
        java.time.OffsetDateTime.parse(dateStr).toInstant()
    }
    val checkInLocal = checkInInstant?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    val autoStopTime = checkInLocal?.withHour(20)?.withMinute(0)?.withSecond(0)
    
    val isWorking = latestCheckIn != null && 
        (latestCheckOut == null || latestCheckIn.date > latestCheckOut.date) &&
        (autoStopTime == null || LocalDateTime.now().isBefore(autoStopTime))

    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isWorking, latestCheckIn) {
        if (isWorking && checkInLocal != null && autoStopTime != null) {
            while (isWorking) {
                val now = LocalDateTime.now()
                val endTime = if (now.isAfter(autoStopTime)) autoStopTime else now
                elapsedTime = java.time.Duration.between(checkInLocal, endTime).seconds
                if (elapsedTime < 0) elapsedTime = 0
                delay(1000)
            }
        } else if (!isWorking && latestCheckIn != null && checkInLocal != null && autoStopTime != null && latestCheckOut == null) {
            elapsedTime = java.time.Duration.between(checkInLocal, autoStopTime).seconds
            if (elapsedTime < 0) elapsedTime = 0
        }
    }

    val hours = elapsedTime / 3600
    val minutes = (elapsedTime % 3600) / 60
    val seconds = elapsedTime % 60
    val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    // Parse schedule
    val scheduleType = position?.scheduleType ?: "none"
    val scheduleNormMinutes = position?.scheduleNormMinutes ?: 480 // default 8h
    val maxSeconds = scheduleNormMinutes * 60f
    
    val scheduleStartStr = position?.scheduleStart?.take(5)
    val scheduleEndStr = position?.scheduleEnd?.take(5)
    
    val scheduleText = when (scheduleType) {
        "rigid" -> "График: $scheduleStartStr - $scheduleEndStr"
        "flexible_daily" -> "Гибкий ($scheduleStartStr - $scheduleEndStr, норма: ${scheduleNormMinutes / 60} ч)"
        "flexible_weekly" -> "Недельная норма: ${scheduleNormMinutes / 60} ч"
        else -> "Без графика (свободное посещение)"
    }

    // Check if today is a working day
    val todayDow = LocalDateTime.now().dayOfWeek.value // 1 (Mon) - 7 (Sun)
    val workingDays = position?.scheduleDays?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    val isWorkDay = scheduleType == "none" || workingDays.isEmpty() || workingDays.contains(todayDow)

    val latenessMinutes = if (checkInLocal != null && scheduleStartStr != null && scheduleType == "rigid") {
        try {
            val (h, m) = scheduleStartStr.split(":").map { it.toInt() }
            val expectedStart = checkInLocal.withHour(h).withMinute(m).withSecond(0)
            if (checkInLocal.isAfter(expectedStart)) java.time.Duration.between(expectedStart, checkInLocal).toMinutes() else 0
        } catch (e: Exception) { 0 }
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Таймер работы") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Рабочий день", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(scheduleText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    val sweep = if (maxSeconds > 0) (elapsedTime % maxSeconds) / maxSeconds * 360f else 0f
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(timeString, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text(if (isWorking) "Работаю" else "Остановлен", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (latenessMinutes > 0) {
                Text(
                    text = "Опоздание $latenessMinutes мин",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.errorContainer, shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else if (scheduleType == "rigid") {
                Text(
                    text = "Вовремя",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f), shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (isWorkDay || isWorking) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val sessionManager = ru.moonlited.pocketmanager.utils.SessionManager(context)

                    if (isWorking) {
                        FloatingActionButton(
                            onClick = { viewModel.checkIn("check_out") },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Text("Стоп", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { viewModel.checkIn("check_in") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Старт")
                        }
                    }

                    if (sessionManager.usePomodoroMethod) {
                        OutlinedButton(onClick = onNavigateToPomodoro) {
                            Text("Помодоро")
                        }
                    }
                }
            } else {
                Text(
                    text = "Сегодня по графику у вас выходной",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
