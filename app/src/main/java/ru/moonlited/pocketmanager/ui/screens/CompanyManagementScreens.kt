package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.moonlited.pocketmanager.data.api.DepartmentResponse
import ru.moonlited.pocketmanager.data.api.PositionResponse
import ru.moonlited.pocketmanager.data.api.UserResponse
import ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModel

@Composable
fun DepartmentsTab(viewModel: CompanyManagementViewModel, onNavigateToSchedule: (Int, Int) -> Unit) {
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
                DepartmentCard(dept, viewModel, onNavigateToSchedule)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DepartmentCard(dept: DepartmentResponse, viewModel: CompanyManagementViewModel, onNavigateToSchedule: (Int, Int) -> Unit) {
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
                            onNavigateToSchedule(dept.id, pos.id)
                        }) {
                            Icon(Icons.Default.Settings, "Настроить график")
                        }
                        IconButton(onClick = {
                            viewModel.updatePosition(dept.id, pos.id, pos.name, pos.hierarchyLevel + 1)
                        }) {
                            Icon(Icons.Default.ArrowUpward, "Повысить уровень")
                        }
                        IconButton(onClick = {
                            viewModel.updatePosition(dept.id, pos.id, pos.name, pos.hierarchyLevel - 1)
                        }) {
                            Icon(Icons.Default.ArrowDownward, "Понизить уровень")
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = newPosName,
                        onValueChange = { newPosName = it },
                        label = { Text("Новая должность", fontSize = 12.sp, maxLines = 1) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (newPosName.isNotBlank()) {
                            viewModel.createPosition(newPosName, dept.id, 0)
                            newPosName = ""
                        }
                    }) {
                        Text("Добавить", fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerManagementTab(viewModel: CompanyManagementViewModel, onNavigateToWorkerStats: (Int) -> Unit) {
    val users by viewModel.users.collectAsState()
    val departments by viewModel.departments.collectAsState()

    var selectedFilterDeptId by remember { mutableStateOf<Int?>(-1) } // -1 = Все
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Сотрудники", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            val filterName = when (selectedFilterDeptId) {
                -1 -> "Все"
                null -> "Без отдела"
                else -> departments.find { it.id == selectedFilterDeptId }?.name ?: "Неизвестный отдел"
            }
            OutlinedTextField(
                value = filterName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Фильтр по отделу") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Все") },
                    onClick = { selectedFilterDeptId = -1; expanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Без отдела") },
                    onClick = { selectedFilterDeptId = null; expanded = false }
                )
                departments.forEach { dept ->
                    DropdownMenuItem(
                        text = { Text(dept.name) },
                        onClick = { selectedFilterDeptId = dept.id; expanded = false }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))

        val filteredUsers = users.filter { 
            if (selectedFilterDeptId == -1) true 
            else it.departmentId == selectedFilterDeptId 
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredUsers) { user ->
                WorkerCard(user, departments, viewModel, onNavigateToWorkerStats)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerCard(user: UserResponse, departments: List<DepartmentResponse>, viewModel: CompanyManagementViewModel, onNavigateToWorkerStats: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedDeptId by remember(user.departmentId) { mutableStateOf(user.departmentId) }
    var selectedPosId by remember(user.positionId) { mutableStateOf(user.positionId) }
    var selectedRole by remember(user.role) { mutableStateOf(user.role) }
    
    var deptExpanded by remember { mutableStateOf(false) }
    var posExpanded by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            val displayRole = when(user.role) {
                "worker" -> "Сотрудник"
                "manager" -> "Менеджер"
                "director" -> "Директор"
                else -> user.role
            }
            Text("Роль: $displayRole", style = MaterialTheme.typography.bodyLarge)
            
            val deptName = departments.find { it.id == user.departmentId }?.name ?: "Не назначен"
            Text("Отдел: $deptName", style = MaterialTheme.typography.bodyLarge)
            
            val posName = departments.find { it.id == user.departmentId }?.positions?.find { it.id == user.positionId }?.name ?: "Нет должности"
            Text("Должность: $posName", style = MaterialTheme.typography.bodyLarge)
            
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (expanded) "Скрыть" else "Настроить", fontSize = 12.sp, maxLines = 1)
                }
                Spacer(Modifier.width(4.dp))
                TextButton(
                    onClick = { 
                        onNavigateToWorkerStats(user.id)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Статистика", fontSize = 12.sp, maxLines = 1)
                }
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = it }
                ) {
                    val currentDeptName = departments.find { it.id == selectedDeptId }?.name ?: "Выберите отдел"
                    OutlinedTextField(
                        value = currentDeptName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Отдел") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept.name) },
                                onClick = {
                                    selectedDeptId = dept.id
                                    selectedPosId = null
                                    deptExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                val currentDept = departments.find { it.id == selectedDeptId }
                ExposedDropdownMenuBox(
                    expanded = posExpanded,
                    onExpandedChange = { posExpanded = it }
                ) {
                    val currentPosName = currentDept?.positions?.find { it.id == selectedPosId }?.name ?: "Выберите должность"
                    OutlinedTextField(
                        value = currentPosName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Должность") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        enabled = currentDept?.positions?.isNotEmpty() == true
                    )
                    ExposedDropdownMenu(
                        expanded = posExpanded,
                        onDismissRequest = { posExpanded = false }
                    ) {
                        currentDept?.positions?.forEach { pos ->
                            DropdownMenuItem(
                                text = { Text(pos.name) },
                                onClick = {
                                    selectedPosId = pos.id
                                    posExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    val displayRoleName = when(selectedRole) {
                        "worker" -> "Сотрудник"
                        "manager" -> "Менеджер"
                        "director" -> "Директор"
                        else -> selectedRole
                    }
                    OutlinedTextField(
                        value = displayRoleName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Роль") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        listOf("worker" to "Сотрудник", "manager" to "Менеджер", "director" to "Директор").forEach { (roleValue, roleName) ->
                            DropdownMenuItem(
                                text = { Text(roleName) },
                                onClick = {
                                    selectedRole = roleValue
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = {
                            viewModel.updateWorker(user.id, selectedDeptId, selectedPosId, selectedRole)
                            expanded = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Сохранить", fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.kickWorker(user.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Исключить", fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
