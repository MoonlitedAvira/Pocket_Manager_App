// ui/screens/TaskListScreen.kt
package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import ru.moonlited.pocketmanager.data.local.entity.TaskEntity
import ru.moonlited.pocketmanager.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.Instant
import java.time.ZoneOffset
import ru.moonlited.pocketmanager.data.api.UserResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: TaskViewModel = koinViewModel(), onOpenDrawer: () -> Unit) {
    val tasks by viewModel.tasks.collectAsState()
    val myTasks by viewModel.myTasks.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val delegatedTasks by viewModel.delegatedTasks.collectAsState()
    val users by viewModel.users.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskDetailsToShow by remember { mutableStateOf<TaskEntity?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val context = androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == "ru.moonlited.pocketmanager.TASKS_UPDATED") {
                    viewModel.syncTasks()
                }
            }
        }
        val filter = android.content.IntentFilter("ru.moonlited.pocketmanager.TASKS_UPDATED")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncTasks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задачи") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Меню")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Мои", fontSize = 12.sp, maxLines = 1) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Делегированные", fontSize = 11.sp, maxLines = 1) }
                )
            }

            if (selectedTabIndex == 0) {
                HeatMapCalendar(
                    tasks = myTasks,
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && filteredTasks.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (filteredTasks.isEmpty()) {
                        Text(
                            text = "На этот день задач нет.",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTasks) { task ->
                                TaskCard(
                                    task = task,
                                    users = users,
                                    onCheckedChange = { if (it) viewModel.completeTask(task.localId) },
                                    onCardClick = { taskDetailsToShow = task }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && delegatedTasks.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (delegatedTasks.isEmpty()) {
                        Text(
                            text = "Нет делегированных задач.",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(delegatedTasks) { taskResponse ->
                                val asEntity = TaskEntity(
                                    localId = taskResponse.id,
                                    remoteId = taskResponse.id,
                                    userId = taskResponse.userId,
                                    title = taskResponse.title,
                                    description = taskResponse.description,
                                    isCompleted = taskResponse.isCompleted,
                                    isDeleted = taskResponse.isDeleted,
                                    createdAt = taskResponse.createdAt,
                                    updatedAt = taskResponse.updatedAt,
                                    isSynced = true,
                                    startExecutionAt = taskResponse.startExecutionAt,
                                    deadline = taskResponse.deadline,
                                    assignedUserId = taskResponse.assignedUserId,
                                    departmentId = taskResponse.departmentId
                                )
                                TaskCard(
                                    task = asEntity,
                                    users = users,
                                    onCheckedChange = { },
                                    onCardClick = { taskDetailsToShow = asEntity }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            CreateTaskDialog(
                viewModel = viewModel,
                initialDate = selectedDate,
                editingTask = null,
                onDismiss = { showAddDialog = false }
            )
        }

        if (taskToEdit != null) {
            CreateTaskDialog(
                viewModel = viewModel,
                initialDate = selectedDate,
                editingTask = taskToEdit,
                onDismiss = { taskToEdit = null }
            )
        }
        
        taskDetailsToShow?.let { task ->
            TaskDetailsDialog(
                task = task,
                users = users,
                currentUserId = viewModel.currentUserId,
                onDismiss = { taskDetailsToShow = null },
                onEditClick = { taskDetailsToShow = null; taskToEdit = task },
                onDeleteClick = { viewModel.deleteTask(task.localId); taskDetailsToShow = null }
            )
        }
    }
}

@Composable
fun HeatMapCalendar(
    tasks: List<TaskEntity>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value - 1
    val daysList = (1..daysInMonth).map { currentMonth.atDay(it) }

    val taskCounts = remember(tasks, currentMonth) {
        val counts = mutableMapOf<LocalDate, Int>()
        tasks.forEach { task ->
            if (!task.isDeleted && !task.isCompleted) {
                val startStr = task.startExecutionAt?.take(10) ?: task.createdAt.take(10)
                val endStr = task.deadline?.take(10) ?: startStr
                try {
                    var date = LocalDate.parse(startStr)
                    val endDate = LocalDate.parse(endStr)
                    while (!date.isAfter(endDate)) {
                        if (date.year == currentMonth.year && date.month == currentMonth.month) {
                            counts[date] = (counts[date] ?: 0) + 1
                        }
                        date = date.plusDays(1)
                    }
                } catch (e: Exception) {}
            }
        }
        counts
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }
            
            val monthName = remember(currentMonth) {
                java.time.format.DateTimeFormatter.ofPattern("LLLL", java.util.Locale.forLanguageTag("ru"))
                    .format(currentMonth).replaceFirstChar { it.uppercase() }
            }
            
            Text(
                text = "$monthName ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            weekDays.forEach {
                Text(text = it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val cells = List(firstDayOfWeek) { null } + daysList
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(cells) { date ->
                if (date == null) {
                    Box(modifier = Modifier.aspectRatio(1f).padding(4.dp))
                } else {
                    val count = taskCounts[date] ?: 0
                    val bgColor = when {
                        count >= 5 -> Color(0xFFE57373)
                        count in 3..4 -> Color(0xFFFFD54F)
                        count in 1..2 -> Color(0xFF81C784)
                        else -> Color.Transparent
                    }

                    val isSelected = date == selectedDate
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = if (bgColor == Color.Transparent) MaterialTheme.colorScheme.onSurface else Color.Black,
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = if (isSelected) TextDecoration.Underline else null,
                            fontSize = if (isSelected) 16.sp else 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    viewModel: TaskViewModel,
    initialDate: LocalDate,
    editingTask: TaskEntity? = null,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(editingTask?.title ?: "") }
    var description by remember { mutableStateOf(editingTask?.description ?: "") }
    var startExecutionAt by remember { mutableStateOf(editingTask?.startExecutionAt?.take(10) ?: initialDate.toString()) }
    var deadline by remember { mutableStateOf(editingTask?.deadline?.take(10) ?: "") }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showDeadlinePicker by remember { mutableStateOf(false) }
    
    val users by viewModel.users.collectAsState()
    val departments by viewModel.departments.collectAsState()

    var selectedUserId by remember { mutableStateOf<Int?>(null) }
    var selectedDeptId by remember { mutableStateOf<Int?>(null) }

    var expandedUser by remember { mutableStateOf(false) }
    var expandedDept by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        startExecutionAt = date.toString()
                    }
                    showStartDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeadlinePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        deadline = date.toString()
                    }
                    showDeadlinePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDeadlinePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingTask != null) "Редактировать задачу" else "Новая задача", fontSize = 20.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = startExecutionAt,
                    onValueChange = {},
                    label = { Text("Дата старта", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deadline,
                    onValueChange = {},
                    label = { Text("Дедлайн", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDeadlinePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDeadlinePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Выбрать дедлайн")
                        }
                    }
                )
                
                if (users.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedUser,
                        onExpandedChange = { expandedUser = !expandedUser }
                    ) {
                        val selectedEmail = users.find { it.id == selectedUserId }?.email ?: "Никто"
                        OutlinedTextField(
                            value = selectedEmail,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Назначить сотруднику") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUser) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedUser, onDismissRequest = { expandedUser = false }) {
                            DropdownMenuItem(text = { Text("Никто") }, onClick = { selectedUserId = null; expandedUser = false })
                            users.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user.email) },
                                    onClick = { selectedUserId = user.id; expandedUser = false }
                                )
                            }
                        }
                    }
                }

                if (departments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedDept,
                        onExpandedChange = { expandedDept = !expandedDept }
                    ) {
                        val selectedDeptName = departments.find { it.id == selectedDeptId }?.name ?: "Никто"
                        OutlinedTextField(
                            value = selectedDeptName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Назначить отделу") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDept) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedDept, onDismissRequest = { expandedDept = false }) {
                            DropdownMenuItem(text = { Text("Никто") }, onClick = { selectedDeptId = null; expandedDept = false })
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.name) },
                                    onClick = { selectedDeptId = dept.id; expandedDept = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalStart = if (startExecutionAt.isNotBlank()) "${startExecutionAt}T00:00:00" else null
                    val finalDeadline = if (deadline.isNotBlank()) "${deadline}T23:59:59" else null
                    if (editingTask != null) {
                        viewModel.editTask(editingTask.localId, title, description, finalStart, finalDeadline)
                    } else {
                        viewModel.addTask(title, description, finalStart, finalDeadline, selectedUserId, selectedDeptId)
                    }
                    onDismiss()
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (editingTask != null) "Сохранить" else "Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun TaskCard(
    task: TaskEntity,
    users: List<UserResponse>,
    onCheckedChange: (Boolean) -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange,
                enabled = !task.isCompleted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (!task.deadline.isNullOrBlank()) {
                    Text(
                        text = "Дедлайн: ${task.deadline.take(10)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                val author = users.find { it.id == task.userId }
                if (author != null) {
                    Text(
                        text = "Назначил(а): ${author.email}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsDialog(
    task: TaskEntity,
    users: List<UserResponse>,
    currentUserId: Int?,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val author = users.find { it.id == task.userId }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Детали задачи", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = task.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                
                if (!task.description.isNullOrBlank()) {
                    HorizontalDivider()
                    Text(text = "Описание:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = task.description, fontSize = 13.sp, style = MaterialTheme.typography.bodyMedium)
                }
                
                if (!task.deadline.isNullOrBlank() || author != null) {
                    HorizontalDivider()
                    if (!task.deadline.isNullOrBlank()) {
                        Text(text = "Дедлайн: ${task.deadline.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (author != null) {
                        Text(text = "Назначил(а): ${author.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentUserId == task.userId) {
                        TextButton(onClick = { onDismiss(); onEditClick() }) { Text("Редактировать", fontSize = 13.sp, maxLines = 1) }
                        TextButton(onClick = { onDismiss(); onDeleteClick() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Удалить", fontSize = 13.sp, maxLines = 1) }
                    }
                }
                TextButton(onClick = onDismiss) { Text("Закрыть", fontSize = 13.sp, maxLines = 1) }
            }
        },
        dismissButton = null
    )
}