package ru.moonlited.pocketmanager.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerWorkerStatsScreen(
    viewModel: CompanyManagementViewModel,
    userId: Int,
    onBack: () -> Unit
) {
    val workerStats by viewModel.workerStats.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadWorkerStats(userId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearWorkerStats()
        }
    }

    val sanHistory = workerStats?.sanResults ?: emptyList()
    val maslachHistory = workerStats?.maslachResults ?: emptyList()
    val munsterbergHistory = workerStats?.munsterbergResults ?: emptyList()
    val isLoading = workerStats == null

    StatsContent(
        sanHistory = sanHistory,
        maslachHistory = maslachHistory,
        munsterbergHistory = munsterbergHistory,
        isLoading = isLoading,
        topBar = { actionsContent ->
            TopAppBar(
                title = { Text("Статистика работника") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = actionsContent
            )
        }
    )
}
