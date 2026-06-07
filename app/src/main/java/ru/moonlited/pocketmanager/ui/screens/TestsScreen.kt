package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToSan: () -> Unit,
    onNavigateToMaslach: () -> Unit,
    onNavigateToMunsterberg: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { ru.moonlited.pocketmanager.utils.SessionManager(context) }
    
    val sanCompleted = sessionManager.isTestCompletedToday("san")
    val maslachCompleted = sessionManager.isTestCompletedToday("maslach")
    val munsterbergCompleted = sessionManager.isTestCompletedToday("munsterberg")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Психологические тесты") },
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
            if (sanCompleted && maslachCompleted && munsterbergCompleted) {
                Text(
                    text = "На сегодня все доступные тесты пройдены. Возвращайтесь завтра!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (!sanCompleted) {
                    Button(
                        onClick = onNavigateToSan,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Тест САН", fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }

                if (!maslachCompleted) {
                    Button(
                        onClick = onNavigateToMaslach,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Тест Маслач (Выгорание)", fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }

                if (!munsterbergCompleted) {
                    Button(
                        onClick = onNavigateToMunsterberg,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Тест Мюнстерберга (Внимание)", fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
