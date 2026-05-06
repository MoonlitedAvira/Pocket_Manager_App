// viewmodel/TaskViewModel.kt
package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.TaskCreateRequest
import ru.moonlited.pocketmanager.data.api.TaskResponse

class TaskViewModel(private val apiService: ApiService) : ViewModel() {

    private val _tasks = MutableStateFlow<List<TaskResponse>>(emptyList())
    val tasks: StateFlow<List<TaskResponse>> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _tasks.value = apiService.getTasks()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val newTask = apiService.createTask(TaskCreateRequest(title = title))
                _tasks.value += newTask
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            try {
                val updatedTask = apiService.completeTask(taskId)
                _tasks.value = _tasks.value.map {
                    if (it.id == taskId) updatedTask else it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun editTask(taskId: Int, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            try {
                val updatedTask = apiService.updateTask(taskId, TaskCreateRequest(title = newTitle))
                _tasks.value = _tasks.value.map {
                    if (it.id == taskId) updatedTask else it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            try {
                apiService.deleteTask(taskId)
                _tasks.value = _tasks.value.filter { it.id != taskId }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class TaskViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}