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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.Room
import ru.moonlited.pocketmanager.data.local.AppDatabase
import ru.moonlited.pocketmanager.data.api.ApiClient
import ru.moonlited.pocketmanager.ui.navigation.*
import ru.moonlited.pocketmanager.ui.screens.*
import ru.moonlited.pocketmanager.ui.theme.PocketManagerTheme
import ru.moonlited.pocketmanager.utils.SessionManager
import org.koin.android.ext.android.inject
import ru.moonlited.pocketmanager.viewmodel.LoginViewModel
import ru.moonlited.pocketmanager.viewmodel.LoginViewModelFactory
import ru.moonlited.pocketmanager.viewmodel.PomodoroViewModel
import ru.moonlited.pocketmanager.viewmodel.PomodoroViewModelFactory
import ru.moonlited.pocketmanager.viewmodel.SanViewModel
import ru.moonlited.pocketmanager.viewmodel.SanViewModelFactory
import ru.moonlited.pocketmanager.viewmodel.ProfileViewModel
import ru.moonlited.pocketmanager.viewmodel.ProfileViewModelFactory
import ru.moonlited.pocketmanager.viewmodel.ManagerCompanyViewModel
import ru.moonlited.pocketmanager.viewmodel.ManagerCompanyViewModelFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxWidth

class MainActivity : ComponentActivity() {
    private val sessionManager: SessionManager by inject()
    private val apiService: ru.moonlited.pocketmanager.data.api.ApiService by inject()
    private val appDatabase: ru.moonlited.pocketmanager.data.local.AppDatabase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val syncRepository: ru.moonlited.pocketmanager.data.repository.SyncRepository by inject()

        val loginViewModelFactory = LoginViewModelFactory(apiService, sessionManager)
        val pomodoroViewModelFactory = PomodoroViewModelFactory(applicationContext, appDatabase, syncRepository, sessionManager)

        val sanViewModelFactory = SanViewModelFactory(appDatabase, syncRepository, sessionManager)
        val profileViewModelFactory = ProfileViewModelFactory(apiService, sessionManager)
        val managerCompanyViewModelFactory = ManagerCompanyViewModelFactory(apiService)
        val companyManagementViewModelFactory = ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModelFactory(apiService)

        val startScreen: Any = if (sessionManager.fetchAuthToken() != null) ProfileRoute else LoginRoute

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

                    val permission = android.Manifest.permission.POST_NOTIFICATIONS
                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                                launcher.launch(permission)
                            }
                        }
                        
                        // Run sync in background on start
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            syncRepository.syncAll()
                        }
                    }

                    val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)
                    val userState by profileViewModel.user.collectAsState()

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentRoute?.contains("LoginRoute") != true,
                        drawerContent = {
                            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.75f)) {
                                Spacer(Modifier.height(32.dp))
                                Text("Меню", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))

                                NavigationDrawerItem(
                                    label = { Text("Мой Профиль", fontSize = 14.sp) },
                                    selected = currentRoute?.contains("ProfileRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(ProfileRoute) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text("Задачи", fontSize = 14.sp) },
                                    selected = currentRoute?.contains("TaskListRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(TaskListRoute) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text("Таймер Pomodoro", fontSize = 14.sp) },
                                    selected = currentRoute?.contains("PomodoroRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(PomodoroRoute) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                                
                                if (userState?.role == "manager" || userState?.role == "director") {
                                    NavigationDrawerItem(
                                        label = { Text("Управление компанией", fontSize = 14.sp) },
                                        selected = currentRoute?.contains("ManagerCompanyRoute") == true,
                                        onClick = { scope.launch { drawerState.close() }; navController.navigate(ManagerCompanyRoute) },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                    )
                                }
                                
                                NavigationDrawerItem(
                                    label = { Text("Психологические тесты", fontSize = 14.sp) },
                                    selected = currentRoute?.contains("TestsRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(TestsRoute) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text("Статистика", fontSize = 14.sp) },
                                    selected = currentRoute?.contains("StatsRoute") == true,
                                    onClick = { scope.launch { drawerState.close() }; navController.navigate(StatsRoute()) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                                Spacer(Modifier.weight(1f))
                                NavigationDrawerItem(
                                    label = { Text("Выйти", color = MaterialTheme.colorScheme.error, fontSize = 14.sp) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { 
                                            drawerState.close() 
                                            sessionManager.clearToken()
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text("Настройки", fontSize = 14.sp) },
                                    selected = currentRoute?.contains("SettingsRoute") == true,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(SettingsRoute)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                            }
                        }
                    ) {
                        val pomodoroViewModel: PomodoroViewModel = viewModel(factory = pomodoroViewModelFactory)
                        val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)
                        val sanViewModel: SanViewModel = viewModel(factory = sanViewModelFactory)
                        val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)

                        LaunchedEffect(Unit) {
                            sessionManager.authEvent.collect { isValid ->
                                if (!isValid) {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        appDatabase.clearAllTables()
                                    }
                                    loginViewModel.resetState()
                                    navController.navigate(LoginRoute) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    profileViewModel.fetchProfile()
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                try {
                                                    apiService.updateFcmToken(ru.moonlited.pocketmanager.data.api.FCMTokenUpdate(token))
                                                } catch (e: Exception) { e.printStackTrace() }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startScreen
                        ) {
                            composable<LoginRoute> {
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = {
                                        profileViewModel.fetchProfile()
                                        navController.navigate(ProfileRoute) {
                                            popUpTo(LoginRoute) { inclusive = true }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate(RegisterRoute)
                                    }
                                )
                            }
                            composable<RegisterRoute> {
                            RegisterScreen(
                                viewModel = loginViewModel,
                                    onRegisterSuccess = {
                                        navController.navigate(RoleSelectionRoute) {
                                            popUpTo(LoginRoute) { inclusive = true }
                                        }
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable<RoleSelectionRoute> {
                                RoleSelectionScreen(
                                    viewModel = loginViewModel,
                                    onRoleSelected = {
                                        profileViewModel.fetchProfile() // Refresh after assigning role
                                        navController.navigate(TaskListRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable<TaskListRoute> {
                                TaskListScreen(
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable<PomodoroRoute> {
                            PomodoroScreen(
                                viewModel = pomodoroViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable<SanTestRoute> {
                                val previousRoute = navController.previousBackStackEntry?.destination?.route
                                val fromWorkStart = previousRoute?.contains("ProfileRoute") == true

                                SanTestScreen(
                                    viewModel = sanViewModel,
                                    fromWorkStart = fromWorkStart,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToStats = { navController.navigate(StatsRoute("SAN")) },
                                    onNavigateToTimer = { navController.navigate(WorkingDayTimerRoute) },
                                    onExit = { navController.popBackStack() }
                                )
                            }
                            composable<TestsRoute> {
                                TestsScreen(
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToSan = { navController.navigate(SanTestRoute) },
                                    onNavigateToMaslach = { navController.navigate(MaslachTestRoute) },
                                    onNavigateToMunsterberg = { navController.navigate(MunsterbergTestRoute) }
                                )
                            }
                            composable<MaslachTestRoute> {
                            MaslachTestScreen(
                                viewModel = sanViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToStats = { navController.navigate(StatsRoute("MASLACH")) }
                                )
                            }
                            composable<MunsterbergTestRoute> {
                            MunsterbergTestScreen(
                                viewModel = sanViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToStats = { navController.navigate(StatsRoute("MUNSTERBERG")) }
                                )
                            }
                            composable<StatsRoute> { backStackEntry ->
                                val route = backStackEntry.toRoute<StatsRoute>()
                                StatsScreen(
                                    viewModel = sanViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    initialTest = route.initialTest
                                )
                            }
                            composable<SettingsRoute> {
                            SettingsScreen(
                                viewModel = pomodoroViewModel,
                                loginViewModel = loginViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToLogin = {
                                        navController.navigate(LoginRoute) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable<ProfileRoute> {
                                ProfileScreen(
                                    viewModel = profileViewModel,
                                    pomodoroViewModel = pomodoroViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToStatistics = { navController.navigate(StatsRoute()) },
                                    onNavigateToSan = { navController.navigate(SanTestRoute) },
                                    onNavigateToWorkingTimer = { navController.navigate(WorkingDayTimerRoute) }
                                )
                            }
                            composable<WorkingDayTimerRoute> {
                                WorkingDayTimerScreen(
                                    viewModel = profileViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToPomodoro = { navController.navigate(PomodoroRoute) }
                                )
                            }
                            composable<ManagerCompanyRoute> {
                                val managerCompanyViewModel: ManagerCompanyViewModel = viewModel(factory = managerCompanyViewModelFactory)
                                val companyManagementViewModel: ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModel = viewModel(factory = companyManagementViewModelFactory)
                                ManagerCompanyScreen(
                                    viewModel = managerCompanyViewModel,
                                    companyViewModel = companyManagementViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onNavigateToSchedule = { deptId, posId -> 
                                        navController.navigate(PositionScheduleRoute(deptId, posId)) 
                                    }
                                )
                            }
                            composable<PositionScheduleRoute> { backStackEntry ->
                                val route = backStackEntry.toRoute<PositionScheduleRoute>()
                                val companyManagementViewModel: ru.moonlited.pocketmanager.viewmodel.CompanyManagementViewModel = viewModel(factory = companyManagementViewModelFactory)
                                ru.moonlited.pocketmanager.ui.screens.PositionScheduleScreen(
                                    departmentId = route.departmentId,
                                    positionId = route.positionId,
                                    viewModel = companyManagementViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}