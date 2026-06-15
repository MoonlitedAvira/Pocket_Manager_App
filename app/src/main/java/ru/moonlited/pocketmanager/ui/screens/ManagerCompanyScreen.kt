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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.moonlited.pocketmanager.viewmodel.ManagerCompanyViewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ManagerCompanyScreen(
    viewModel: ManagerCompanyViewModel,
    companyViewModel: ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModel,
    onOpenDrawer: () -> Unit
) {
    val invitations by viewModel.invitations.collectAsState()

    LaunchedEffect(Unit) {
        companyViewModel.loadData()
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Работники", "Структура", "Инвайты")

    var departmentIdInput by remember { mutableStateOf("") }
    var positionIdInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление компанией") },
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
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> WorkerManagementTab(companyViewModel)
                1 -> DepartmentsTab(companyViewModel)
                2 -> Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Сгенерировать приглашение", style = MaterialTheme.typography.titleLarge)
            
            OutlinedTextField(
                value = departmentIdInput,
                onValueChange = { departmentIdInput = it },
                label = { Text("ID Отдела (опционально)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = positionIdInput,
                onValueChange = { positionIdInput = it },
                label = { Text("ID Должности (опционально)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val deptId = departmentIdInput.toIntOrNull()
                    val posId = positionIdInput.toIntOrNull()
                    viewModel.generateInvite(deptId, posId)
                    departmentIdInput = ""
                    positionIdInput = ""
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Сгенерировать код")
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text("Созданные приглашения", style = MaterialTheme.typography.titleMedium)
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(invitations) { invite ->
                    var showDialog by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Код приглашения") },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    val qrBitmap = remember(invite.code) { ru.moonlited.pocketmanager.utils.generateQrCode(invite.code, 600) }
                                    if (qrBitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "QR Code",
                                            modifier = Modifier.size(200.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    Text(invite.code, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clipData = android.content.ClipData.newPlainText("text", invite.code)
                                    clipboardManager.setPrimaryClip(clipData)
                                    android.widget.Toast.makeText(context, "Код скопирован", android.widget.Toast.LENGTH_SHORT).show()
                                    showDialog = false
                                }) { Text("Скопировать") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showDialog = false
                                    viewModel.deleteInvite(invite.code)
                                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                            }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDialog = true }
                    ) {
                        Box {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Код: ${invite.code}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                if (invite.departmentId != null) {
                                    Text("Отдел ID: ${invite.departmentId}")
                                }
                                if (invite.positionId != null) {
                                    Text("Должность ID: ${invite.positionId}")
                                }
                            }
                        }
                    }
                        }
                    }
                }
            }
        }
    }
}
