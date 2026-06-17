package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionScheduleScreen(
    departmentId: Int,
    positionId: Int,
    viewModel: CompanyManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val departments by viewModel.departments.collectAsState()
    val department = departments.find { it.id == departmentId }
    val position = department?.positions?.find { it.id == positionId }

    var scheduleType by remember(position) { mutableStateOf(position?.scheduleType ?: "none") }
    var scheduleStart by remember(position) { mutableStateOf(position?.scheduleStart?.take(5) ?: "") }
    var scheduleEnd by remember(position) { mutableStateOf(position?.scheduleEnd?.take(5) ?: "") }
    var scheduleNormHours by remember(position) { mutableStateOf((position?.scheduleNormMinutes ?: 0) / 60) }

    val initialDays = position?.scheduleDays?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    var selectedDays by remember(position) { mutableStateOf(initialDays) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("График: ${position?.name ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Тип графика", style = MaterialTheme.typography.titleMedium)

            val options = listOf(
                "none" to "Свободное посещение (без графика)",
                "rigid" to "Жесткий график (фиксированное время)",
                "flexible_daily" to "Гибкий дневной (с границами)",
                "flexible_weekly" to "Гибкий недельный"
            )

            options.forEach { (type, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (type == scheduleType),
                            onClick = { scheduleType = type },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (type == scheduleType),
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }

            if (scheduleType != "none") {
                Text("Рабочие дни", style = MaterialTheme.typography.titleMedium)
                val daysOfWeek = listOf(
                    1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс"
                )
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysOfWeek.forEach { (dayId, label) ->
                        FilterChip(
                            selected = selectedDays.contains(dayId),
                            onClick = {
                                selectedDays = if (selectedDays.contains(dayId)) {
                                    selectedDays - dayId
                                } else {
                                    selectedDays + dayId
                                }
                            },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            if (scheduleType == "rigid" || scheduleType == "flexible_daily") {
                Box(modifier = Modifier.fillMaxWidth().clickable { showStartPicker = true }) {
                    OutlinedTextField(
                        value = scheduleStart,
                        onValueChange = { },
                        label = { Text(if (scheduleType == "rigid") "Время начала (ЧЧ:ММ)" else "Нижняя граница прихода (ЧЧ:ММ)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().clickable { showEndPicker = true }) {
                    OutlinedTextField(
                        value = scheduleEnd,
                        onValueChange = { },
                        label = { Text(if (scheduleType == "rigid") "Время конца (ЧЧ:ММ)" else "Верхняя граница ухода (ЧЧ:ММ)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            if (scheduleType == "flexible_daily" || scheduleType == "flexible_weekly") {
                OutlinedTextField(
                    value = scheduleNormHours.toString(),
                    onValueChange = { scheduleNormHours = it.toIntOrNull() ?: 0 },
                    label = { Text(if (scheduleType == "flexible_daily") "Норма часов в день" else "Норма часов в неделю") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val start = if (scheduleStart.isNotBlank()) "$scheduleStart:00" else null
                    val end = if (scheduleEnd.isNotBlank()) "$scheduleEnd:00" else null
                    val daysStr = if (selectedDays.isNotEmpty()) selectedDays.sorted().joinToString(",") else null
                    var normMinutes: Int? = null

                    if (scheduleType == "flexible_daily" || scheduleType == "flexible_weekly") {
                        normMinutes = scheduleNormHours * 60
                    } else if (scheduleType == "rigid" && scheduleStart.length >= 5 && scheduleEnd.length >= 5) {
                        try {
                            val (sh, sm) = scheduleStart.split(":").map { it.toInt() }
                            val (eh, em) = scheduleEnd.split(":").map { it.toInt() }
                            normMinutes = (eh * 60 + em) - (sh * 60 + sm)
                        } catch (e: Exception) {
                            normMinutes = null
                        }
                    }

                    viewModel.updatePosition(
                        deptId = departmentId,
                        posId = positionId,
                        scheduleType = scheduleType,
                        scheduleDays = if (scheduleType == "none") null else daysStr,
                        scheduleStart = start,
                        scheduleEnd = end,
                        scheduleNormMinutes = normMinutes
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }

    if (showStartPicker) {
        TimePickerDialog(
            title = "Выберите время начала",
            onDismiss = { showStartPicker = false },
            onTimeSelected = { h, m ->
                scheduleStart = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }
        )
    }

    if (showEndPicker) {
        TimePickerDialog(
            title = "Выберите время конца",
            onDismiss = { showEndPicker = false },
            onTimeSelected = { h, m ->
                scheduleEnd = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                    onDismiss()
                }
            ) {
                Text("ОК")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
