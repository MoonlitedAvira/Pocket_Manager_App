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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.moonlited.pocketmanager.viewmodel.ProfileViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingDayTimerScreen(
    viewModel: ProfileViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToPomodoro: () -> Unit
) {
    val attendances by viewModel.attendances.collectAsState()

    val latestCheckIn = attendances.filter { it.actionType == "check_in" }.maxByOrNull { it.date }
    val latestCheckOut = attendances.filter { it.actionType == "check_out" }.maxByOrNull { it.date }
    
    // Сервер PostgreSQL отдаёт время без зоны (naive), которое соответствует часовому поясу сервера (UTC+3).
    // Мы явно указываем +03:00, чтобы получить правильный Instant.
    val checkInInstant = latestCheckIn?.date?.let { 
        val dateStr = if (!it.endsWith("Z") && !it.contains("+")) it + "+03:00" else it
        java.time.OffsetDateTime.parse(dateStr).toInstant()
    }
    val checkInLocal = checkInInstant?.atZone(java.time.ZoneId.systemDefault())?.toLocalDateTime()
    val autoStopTime = checkInLocal?.withHour(20)?.withMinute(0)?.withSecond(0)
    
    val isWorking = latestCheckIn != null && 
        (latestCheckOut == null || latestCheckIn.date > latestCheckOut.date) &&
        (autoStopTime == null || java.time.LocalDateTime.now().isBefore(autoStopTime))

    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isWorking, latestCheckIn) {
        if (isWorking && checkInLocal != null && autoStopTime != null) {
            while (isWorking) {
                val now = java.time.LocalDateTime.now()
                val endTime = if (now.isAfter(autoStopTime)) autoStopTime else now
                elapsedTime = java.time.Duration.between(checkInLocal, endTime).seconds
                if (elapsedTime < 0) elapsedTime = 0
                delay(1000)
            }
        } else if (!isWorking && latestCheckIn != null && checkInLocal != null && autoStopTime != null && latestCheckOut == null) {
            // Auto-stopped state
            elapsedTime = java.time.Duration.between(checkInLocal, autoStopTime).seconds
            if (elapsedTime < 0) elapsedTime = 0
        }
    }

    val hours = elapsedTime / 3600
    val minutes = (elapsedTime % 3600) / 60
    val seconds = elapsedTime % 60
    val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val latenessMinutes = if (checkInLocal != null) {
        val expectedStart = checkInLocal.withHour(9).withMinute(0).withSecond(0)
        if (checkInLocal.isAfter(expectedStart)) java.time.Duration.between(expectedStart, checkInLocal).toMinutes() else 0
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
            Text("График: 09:00 - 18:00", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
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
                    
                    val sweep = (elapsedTime % 28800) / 28800f * 360f
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
            } else {
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
        }
    }
}
