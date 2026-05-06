// MainActivity.kt
package ru.moonlited.pocketmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.ApiClient
import ru.moonlited.pocketmanager.ui.navigation.*
import ru.moonlited.pocketmanager.ui.screens.*
import ru.moonlited.pocketmanager.ui.theme.PocketManagerTheme
import ru.moonlited.pocketmanager.utils.SessionManager
import ru.moonlited.pocketmanager.viewmodel.LoginViewModel
import ru.moonlited.pocketmanager.viewmodel.LoginViewModelFactory
import ru.moonlited.pocketmanager.viewmodel.TaskViewModel
import ru.moonlited.pocketmanager.viewmodel.TaskViewModelFactory
import ru.moonlited.pocketmanager.viewmodel.PomodoroViewModel
import ru.moonlited.pocketmanager.viewmodel.PomodoroViewModelFactory
import ru.moonlited.pocketmanager.viewmodel.SanViewModel
import ru.moonlited.pocketmanager.viewmodel.SanViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        val apiService = ApiClient.create(sessionManager)
        val loginViewModelFactory = LoginViewModelFactory(apiService, sessionManager)
        val taskViewModelFactory = TaskViewModelFactory(apiService)
        val pomodoroViewModelFactory = PomodoroViewModelFactory(apiService)
        val sanViewModelFactory = SanViewModelFactory(apiService)

        val startScreen: Any = if (sessionManager.fetchAuthToken() != null) TaskListRoute else LoginRoute

        setContent {
            PocketManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentRoute?.contains("LoginRoute") != true,
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(Modifier.height(32.dp))
                                Text("Меню", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))

                                NavigationDrawerItem(
                                    label = { Text("Мои задачи") },
                                    selected = currentRoute?.contains("TaskListRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(TaskListRoute) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text("Таймер Pomodoro") },
                                    selected = currentRoute?.contains("PomodoroRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(PomodoroRoute) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text("Тест САН") },
                                    selected = currentRoute?.contains("SanTestRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(SanTestRoute) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                                Spacer(Modifier.weight(1f))
                                NavigationDrawerItem(
                                    label = { Text("Выйти", color = MaterialTheme.colorScheme.error) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        sessionManager.clearToken()
                                        navController.navigate(LoginRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp)
                                )
                            }
                        }
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = startScreen
                        ) {
                            composable<LoginRoute> {
                                val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = {
                                        navController.navigate(TaskListRoute) {
                                            popUpTo(LoginRoute) { inclusive = true }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate(RegisterRoute)
                                    }
                                )
                            }
                            composable<RegisterRoute> {
                                val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)
                                RegisterScreen(
                                    viewModel = loginViewModel,
                                    onRegisterSuccess = {
                                        navController.navigate(TaskListRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable<TaskListRoute> {
                                val taskViewModel: TaskViewModel = viewModel(factory = taskViewModelFactory)
                                TaskListScreen(
                                    viewModel = taskViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable<PomodoroRoute> {
                                val pomodoroViewModel: PomodoroViewModel = viewModel(factory = pomodoroViewModelFactory)
                                PomodoroScreen(
                                    viewModel = pomodoroViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable<SanTestRoute> {
                                val sanViewModel: SanViewModel = viewModel(factory = sanViewModelFactory)
                                SanTestScreen(
                                    viewModel = sanViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}