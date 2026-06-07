// viewmodel/TaskViewModel.kt
package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.local.entity.TaskEntity
import ru.moonlited.pocketmanager.data.repository.TaskRepository
import ru.moonlited.pocketmanager.data.api.DepartmentResponse
import ru.moonlited.pocketmanager.data.api.UserResponse
import java.time.LocalDate
import ru.moonlited.pocketmanager.utils.SessionManager

class TaskViewModel(private val taskRepository: TaskRepository, private val sessionManager: SessionManager) : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks

    val currentUserId: Int? get() = sessionManager.getMyId()

    private val _delegatedTasks = MutableStateFlow<List<ru.moonlited.pocketmanager.data.api.TaskResponse>>(emptyList())
    val delegatedTasks: StateFlow<List<ru.moonlited.pocketmanager.data.api.TaskResponse>> = _delegatedTasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    val users: StateFlow<List<UserResponse>> = _users

    private val _departments = MutableStateFlow<List<DepartmentResponse>>(emptyList())
    val departments: StateFlow<List<DepartmentResponse>> = _departments

    val myTasks: StateFlow<List<TaskEntity>> = _tasks.map { tasks ->
        val myId = sessionManager.getMyId()
        tasks.filter { task ->
            if (task.isDeleted) return@filter false
            
            // Если задачу создал я, и она назначена кому-то другому, она не должна быть во вкладке "Мои"
            if (task.userId == myId && (task.assignedUserId != null || task.departmentId != null) && task.assignedUserId != myId) {
                return@filter false
            }
            true
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(myTasks, _selectedDate) { tasks, date ->
        tasks.filter { task ->
            val startStr = task.startExecutionAt?.take(10) ?: task.createdAt.take(10)
            val endStr = task.deadline?.take(10) ?: startStr
            
            try {
                val startDate = LocalDate.parse(startStr)
                val endDate = LocalDate.parse(endStr)
                !date.isBefore(startDate) && !date.isAfter(endDate)
            } catch (e: Exception) {
                // Fallback to strict string match if parsing fails
                task.deadline?.startsWith(date.toString()) == true || 
                task.startExecutionAt?.startsWith(date.toString()) == true || 
                task.createdAt.startsWith(date.toString())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            taskRepository.getTasks().collectLatest { taskList ->
                _tasks.value = taskList
            }
        }
        
        // Очистка при выходе из аккаунта
        viewModelScope.launch {
            sessionManager.authEvent.collectLatest { isLoggedIn ->
                if (!isLoggedIn) {
                    _tasks.value = emptyList()
                    _delegatedTasks.value = emptyList()
                    _users.value = emptyList()
                    _departments.value = emptyList()
                }
            }
        }
        syncTasks()
    }

    fun syncTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            ru.moonlited.pocketmanager.utils.AppLogger.d("TaskViewModel: Starting syncTasks")
            taskRepository.syncTasks()
            ru.moonlited.pocketmanager.utils.AppLogger.d("TaskViewModel: syncTasks finished, fetching lists")
            _users.value = taskRepository.getUsers()
            _departments.value = taskRepository.getDepartments()
            _delegatedTasks.value = taskRepository.getDelegatedTasks()
            ru.moonlited.pocketmanager.utils.AppLogger.d("TaskViewModel: Delegated tasks count: ${_delegatedTasks.value.size}")
            _isLoading.value = false
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addTask(
        title: String,
        description: String? = null,
        startExecutionAt: String? = null,
        deadline: String? = null,
        assignedUserId: Int? = null,
        departmentId: Int? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.createTask(title, description, startExecutionAt, deadline, assignedUserId, departmentId)
            syncTasks()
        }
    }

    fun completeTask(localId: Int) {
        viewModelScope.launch {
            taskRepository.completeTask(localId)
            syncTasks()
        }
    }

    fun editTask(localId: Int, newTitle: String, newDescription: String?, newStartExecutionAt: String?, newDeadline: String?) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            taskRepository.editTask(localId, newTitle, newDescription, newStartExecutionAt, newDeadline)
            syncTasks()
        }
    }

    fun deleteTask(localId: Int) {
        viewModelScope.launch {
            taskRepository.deleteTask(localId)
            syncTasks()
        }
    }
}