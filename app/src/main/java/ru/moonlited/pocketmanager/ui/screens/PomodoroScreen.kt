package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.moonlited.pocketmanager.viewmodel.PomodoroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(viewModel: PomodoroViewModel, onOpenDrawer: () -> Unit) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val currentState by viewModel.currentState.collectAsState()
    val currentCycle by viewModel.currentCycle.collectAsState()
    val totalTime by viewModel.totalTimeForState.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current

    val minutes = (timeLeft / 60).toString().padStart(2, '0')
    val seconds = (timeLeft % 60).toString().padStart(2, '0')

    val isWork = currentState == "WORK"
    val color = if (isWork) androidx.compose.ui.graphics.Color(0xFFFFC107) else androidx.compose.ui.graphics.Color(0xFF4CAF50)
    
    val progress = if (totalTime > 0) {
        if (isWork) {
            timeLeft.toFloat() / totalTime
        } else {
            1f - (timeLeft.toFloat() / totalTime)
        }
    } else 0f

    val totalCycles by viewModel.totalCycles.collectAsState()

    val stateText = when (currentState) {
        "WORK" -> "Работаем"
        "SHORT_BREAK" -> "Отдыхаем"
        "LONG_BREAK" -> "Большой отдых"
        else -> "Ожидание"
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (context.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                (context as? android.app.Activity)?.requestPermissions(arrayOf(permission), 101)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Помодоро") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stateText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Сессия: $currentCycle/$totalCycles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(280.dp),
                    color = color,
                    strokeWidth = 16.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$minutes:$seconds",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(
                    onClick = { viewModel.toggleTimer() }, 
                    modifier = Modifier.width(140.dp).height(50.dp)
                ) {
                    Text(if (isRunning) "Пауза" else if (timeLeft == 0) "След." else "Старт", fontSize = 18.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.resetTimer() }, 
                    modifier = Modifier.width(140.dp).height(50.dp)
                ) {
                    Text("Сброс", fontSize = 18.sp)
                }
            }
        }
    }
}