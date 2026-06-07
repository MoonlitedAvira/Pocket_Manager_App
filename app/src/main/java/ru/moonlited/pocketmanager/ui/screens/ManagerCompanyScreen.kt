package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.moonlited.pocketmanager.viewmodel.ManagerCompanyViewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ManagerCompanyScreen(
    viewModel: ManagerCompanyViewModel,
    onOpenDrawer: () -> Unit
) {
    val invitations by viewModel.invitations.collectAsState()

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
                .padding(16.dp),
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
                    var showMenu by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clipData = android.content.ClipData.newPlainText("text", invite.code)
                                    clipboardManager.setPrimaryClip(clipData)
                                    android.widget.Toast.makeText(context, "Код скопирован", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onLongClick = {
                                    showMenu = true
                                }
                            )
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
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.deleteInvite(invite.code)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
