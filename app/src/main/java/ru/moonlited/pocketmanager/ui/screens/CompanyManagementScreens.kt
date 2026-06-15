package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.moonlited.pocketmanager.data.api.DepartmentResponse
import ru.moonlited.pocketmanager.data.api.PositionResponse
import ru.moonlited.pocketmanager.data.api.UserResponse
import ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModel

@Composable
fun DepartmentsTab(viewModel: CompanyManagementViewModel) {
    val departments by viewModel.departments.collectAsState()
    var newDeptName by remember { mutableStateOf("") }
    
    // Server requires company_id, but the UI might not know it initially unless we take it from a user or pass it.
    // For simplicity, we assume companyId is known or 0 if server overrides it.
    // However, if the server requires it, we must get it from the user list or something.
    val companyId = departments.firstOrNull()?.companyId ?: 0

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Отделы и должности", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newDeptName,
                onValueChange = { newDeptName = it },
                label = { Text("Название нового отдела") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (newDeptName.isNotBlank()) {
                    viewModel.createDepartment(newDeptName, companyId)
                    newDeptName = ""
                }
            }) {
                Text("Создать")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn {
            items(departments) { dept ->
                DepartmentCard(dept, viewModel)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DepartmentCard(dept: DepartmentResponse, viewModel: CompanyManagementViewModel) {
    var newPosName by remember { mutableStateOf("") }
    var showPositions by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(dept.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { showPositions = !showPositions }) {
                    Text(if (showPositions) "Скрыть" else "Должности")
                }
            }
            
            if (showPositions) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                // Sort positions by hierarchy_level descending
                val sortedPositions = dept.positions.sortedByDescending { it.hierarchyLevel }
                
                sortedPositions.forEachIndexed { index, pos ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${pos.name} (Уровень: ${pos.hierarchyLevel})", modifier = Modifier.weight(1f))
                        
                        IconButton(onClick = {
                            viewModel.updatePosition(pos.id, pos.name, pos.hierarchyLevel + 1)
                        }) {
                            Icon(Icons.Default.ArrowUpward, "Повысить уровень")
                        }
                        IconButton(onClick = {
                            viewModel.updatePosition(pos.id, pos.name, pos.hierarchyLevel - 1)
                        }) {
                            Icon(Icons.Default.ArrowDownward, "Понизить уровень")
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = newPosName,
                        onValueChange = { newPosName = it },
                        label = { Text("Новая должность") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (newPosName.isNotBlank()) {
                            viewModel.createPosition(newPosName, dept.id, 0)
                            newPosName = ""
                        }
                    }) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerManagementTab(viewModel: CompanyManagementViewModel) {
    val users by viewModel.users.collectAsState()
    val departments by viewModel.departments.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Сотрудники", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
        }
        items(users) { user ->
            WorkerCard(user, departments, viewModel)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun WorkerCard(user: UserResponse, departments: List<DepartmentResponse>, viewModel: CompanyManagementViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selectedDeptId by remember(user.departmentId) { mutableStateOf(user.departmentId) }
    var selectedPosId by remember(user.positionId) { mutableStateOf(user.positionId) }
    var selectedRole by remember(user.role) { mutableStateOf(user.role) }
    
    var showStats by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.email, fontWeight = FontWeight.Bold)
            Text("Роль: ${user.role}")
            
            val deptName = departments.find { it.id == user.departmentId }?.name ?: "Не назначен"
            Text("Отдел: $deptName")
            
            val posName = departments.find { it.id == user.departmentId }?.positions?.find { it.id == user.positionId }?.name ?: "Нет должности"
            Text("Должность: $posName")
            
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Скрыть настройки" else "Настроить")
                }
                TextButton(onClick = { 
                    viewModel.loadWorkerStats(user.id)
                    showStats = true 
                }) {
                    Text("Статистика")
                }
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Department Selection (Simple buttons for now to avoid dropdown complexities if desired, but let's use a simple list of buttons)
                Text("Выбрать отдел:")
                departments.forEach { dept ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedDeptId == dept.id, onClick = { 
                            selectedDeptId = dept.id
                            selectedPosId = null // Reset position when changing department
                        })
                        Text(dept.name)
                    }
                }
                
                val currentDept = departments.find { it.id == selectedDeptId }
                if (currentDept != null && currentDept.positions.isNotEmpty()) {
                    Text("Выбрать должность:")
                    currentDept.positions.forEach { pos ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedPosId == pos.id, onClick = { selectedPosId = pos.id })
                            Text(pos.name)
                        }
                    }
                }
                
                Text("Права доступа (Роль):")
                listOf("worker", "manager", "director").forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                        Text(role)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = {
                        viewModel.updateWorker(user.id, selectedDeptId, selectedPosId, selectedRole)
                        expanded = false
                    }) {
                        Text("Сохранить")
                    }
                    OutlinedButton(onClick = {
                        viewModel.kickWorker(user.id)
                    }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Исключить")
                    }
                }
            }
        }
    }
    
    if (showStats) {
        val stats by viewModel.workerStats.collectAsState()
        
        AlertDialog(
            onDismissRequest = { 
                showStats = false 
                viewModel.clearWorkerStats()
            },
            title = { Text("Статистика: ${user.email}") },
            text = {
                if (stats == null) {
                    Text("Загрузка или нет доступа...")
                } else {
                    // Re-use StatsCharts if possible. Let's just do a simple list or charts.
                    // For brevity, we'll just show the latest values.
                    Column {
                        Text("Последние тесты:", fontWeight = FontWeight.Bold)
                        stats?.sanResults?.lastOrNull()?.let {
                            Text("САН: ${it.scoreS}, ${it.scoreA}, ${it.scoreN}")
                        }
                        stats?.maslachResults?.lastOrNull()?.let {
                            Text("Маслач: ЭИ=${it.emotionalExhaustion}, ДП=${it.depersonalization}")
                        }
                        stats?.munsterbergResults?.lastOrNull()?.let {
                            Text("Мюнстерберг: Ошибки=${it.errors}, Время=${it.timeSpentSeconds}c")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showStats = false
                    viewModel.clearWorkerStats()
                }) { Text("Закрыть") }
            }
        )
    }
}
