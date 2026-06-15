// ui/screens/RoleSelectionScreen.kt
package ru.moonlited.pocketmanager.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.moonlited.pocketmanager.ui.components.QrScannerOverlay
import ru.moonlited.pocketmanager.viewmodel.LoginState
import ru.moonlited.pocketmanager.viewmodel.LoginViewModel

@Composable
fun RoleSelectionScreen(
    viewModel: LoginViewModel,
    onRoleSelected: () -> Unit
) {
    var showManagerDialog by remember { mutableStateOf(false) }
    var showWorkerDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        }
    }
    
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            viewModel.resetState()
            onRoleSelected()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Выберите вашу роль",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { showManagerDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            enabled = loginState !is LoginState.Loading
        ) {
            Text("Менеджер\n(Создать компанию)", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 14.sp, maxLines = 2)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showWorkerDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            enabled = loginState !is LoginState.Loading
        ) {
            Text("Работник\n(Войти по инвайту)", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 14.sp, maxLines = 2)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { 
                viewModel.createCompany("Моя компания")
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            enabled = loginState !is LoginState.Loading
        ) {
            Text("Самозанятый", fontSize = 14.sp)
        }

        if (loginState is LoginState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        if (loginState is LoginState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (loginState as LoginState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showManagerDialog) {
        AlertDialog(
            onDismissRequest = { showManagerDialog = false; input = "" },
            title = { Text("Создание компании") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Название компании") }
                )
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.createCompany(input)
                    showManagerDialog = false
                    input = ""
                }) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showManagerDialog = false; input = "" }) { Text("Отмена") }
            }
        )
    }

    if (showWorkerDialog) {
        AlertDialog(
            onDismissRequest = { showWorkerDialog = false; input = "" },
            title = { Text("Вступление в компанию") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Инвайт код") },
                    trailingIcon = {
                        IconButton(onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Сканировать QR")
                        }
                    }
                )
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.joinCompany(input)
                    showWorkerDialog = false
                    input = ""
                }) { Text("Войти") }
            },
            dismissButton = {
                TextButton(onClick = { showWorkerDialog = false; input = "" }) { Text("Отмена") }
            }
        )
    }
    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                QrScannerOverlay { scannedCode ->
                    input = scannedCode
                    showScanner = false
                }
                IconButton(
                    onClick = { showScanner = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Text("Закрыть", color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
