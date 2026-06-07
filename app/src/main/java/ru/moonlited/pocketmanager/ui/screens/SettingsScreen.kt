package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.moonlited.pocketmanager.viewmodel.LoginViewModel
import ru.moonlited.pocketmanager.viewmodel.PomodoroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PomodoroViewModel,
    loginViewModel: LoginViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = ru.moonlited.pocketmanager.utils.SessionManager(context)
    
    var work by remember { mutableStateOf(sessionManager.pomodoroWorkDuration) }
    var shortB by remember { mutableStateOf(sessionManager.pomodoroShortBreak) }
    var longB by remember { mutableStateOf(sessionManager.pomodoroLongBreak) }
    var cycles by remember { mutableStateOf(sessionManager.pomodoroCycles) }
    var enableLong by remember { mutableStateOf(sessionManager.pomodoroEnableLongBreak) }
    var debugMode by remember { mutableStateOf(sessionManager.pomodoroIsDebugMode) }
    var usePomodoro by remember { mutableStateOf(sessionManager.usePomodoroMethod) }

    var showTooltip by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val loginState by loginViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is ru.moonlited.pocketmanager.viewmodel.LoginState.AccountDeleted) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "Меню") }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = {
                        sessionManager.pomodoroWorkDuration = work
                        sessionManager.pomodoroShortBreak = shortB
                        sessionManager.pomodoroLongBreak = longB
                        sessionManager.pomodoroCycles = cycles
                        sessionManager.pomodoroEnableLongBreak = enableLong
                        sessionManager.pomodoroIsDebugMode = debugMode
                        sessionManager.usePomodoroMethod = usePomodoro
                        viewModel.isDebugMode = debugMode
                        viewModel.resetTimer()
                        android.widget.Toast.makeText(context, "Настройки сохранены", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Сохранить настройки")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Таймер Pomodoro", style = MaterialTheme.typography.titleMedium, fontSize = 16.sp)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = usePomodoro, onCheckedChange = { usePomodoro = it })
                Text("Использовать метод Помодоро", fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Работа", fontSize = 12.sp)
                    NumberCounter(value = work, onValueChange = { work = it }, min = 1, max = 120, enabled = usePomodoro)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Перерыв", fontSize = 12.sp)
                    NumberCounter(value = shortB, onValueChange = { shortB = it }, min = 1, max = 60, enabled = usePomodoro)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Отдых", fontSize = 12.sp)
                    NumberCounter(value = longB, onValueChange = { longB = it }, min = 1, max = 60, enabled = usePomodoro)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = enableLong, onCheckedChange = { enableLong = it }, enabled = usePomodoro)
                Text("Включить длинный перерыв", fontSize = 12.sp, color = if (usePomodoro) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Цикличность (кол-во рабочих)", fontSize = 12.sp, color = if (usePomodoro && enableLong) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                IconButton(onClick = { showTooltip = !showTooltip }, enabled = usePomodoro && enableLong) {
                    Icon(Icons.Default.Info, contentDescription = "Информация", tint = if (usePomodoro && enableLong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            HorizontalNumberCounter(value = cycles, onValueChange = { cycles = it }, min = 1, max = 10, enabled = usePomodoro && enableLong)

            if (showTooltip && enableLong && usePomodoro) {
                Text(
                    text = "Цикличность определяет, сколько рабочих циклов должно пройти перед длинным отдыхом.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // TODO: Удалить в предрелизе
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Удалить аккаунт")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showDeleteDialog) {
            var confirmText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удалить аккаунт?", color = MaterialTheme.colorScheme.error) },
                text = {
                    Column {
                        Text("Ваш аккаунт будет помечен как удалённый. Вы можете восстановить его в течение 180 дней, войдя в приложение. Для подтверждения напишите ПОДТВЕРДИТЬ.")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmText,
                            onValueChange = { confirmText = it },
                            placeholder = { Text("ПОДТВЕРДИТЬ") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (confirmText == "ПОДТВЕРДИТЬ") {
                                loginViewModel.deleteAccount()
                                showDeleteDialog = false
                            }
                        },
                        enabled = confirmText == "ПОДТВЕРДИТЬ",
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
                }
            )
        }
    }
}

@Composable
fun RepeatingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val currentClickListener by rememberUpdatedState(onClick)
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = modifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown()
                currentClickListener()
                val job = coroutineScope.launch {
                    delay(400)
                    while (true) {
                        currentClickListener()
                        delay(100)
                    }
                }
                waitForUpOrCancellation()
                job.cancel()
            }
        },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun NumberCounter(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 999,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        RepeatingIconButton(
            onClick = { if (value < max) onValueChange(value + 1) },
            enabled = enabled,
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowUp, contentDescription = "Вверх", tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        
        var textValue by remember(value) { mutableStateOf(value.toString()) }
        
        androidx.compose.foundation.text.BasicTextField(
            value = textValue,
            onValueChange = { newValue ->
                textValue = newValue
                val intVal = newValue.toIntOrNull()
                if (intVal != null && intVal in min..max) {
                    onValueChange(intVal)
                }
            },
            enabled = enabled,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.width(60.dp).padding(vertical = 4.dp)
        )
        
        RepeatingIconButton(
            onClick = { if (value > min) onValueChange(value - 1) },
            enabled = enabled,
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
        ) {
            Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowDown, contentDescription = "Вниз", tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun HorizontalNumberCounter(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 999,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable(enabled = enabled) { if (value > min) onValueChange(value - 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 12.dp).widthIn(min = 24.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable(enabled = enabled) { if (value < max) onValueChange(value + 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}
