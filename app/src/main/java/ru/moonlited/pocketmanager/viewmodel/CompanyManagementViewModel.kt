package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.*

class CompanyManagementViewModel(private val apiService: ApiService) : ViewModel() {

    private val _departments = MutableStateFlow<List<DepartmentResponse>>(emptyList())
    val departments: StateFlow<List<DepartmentResponse>> = _departments

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    val users: StateFlow<List<UserResponse>> = _users

    private val _workerStats = MutableStateFlow<WorkerStatsResponse?>(null)
    val workerStats: StateFlow<WorkerStatsResponse?> = _workerStats

    fun loadData() {
        viewModelScope.launch {
            try {
                _departments.value = apiService.getDepartments()
                _users.value = apiService.getUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createDepartment(name: String, companyId: Int) {
        viewModelScope.launch {
            try {
                apiService.createDepartment(DepartmentCreateRequest(name, companyId)) 
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createPosition(name: String, departmentId: Int, hierarchyLevel: Int) {
        viewModelScope.launch {
            try {
                apiService.createPosition(PositionCreateRequest(name, departmentId, hierarchyLevel))
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePosition(
        deptId: Int, 
        posId: Int, 
        name: String? = null, 
        hierarchyLevel: Int? = null,
        scheduleType: String? = null,
        scheduleDays: String? = null,
        scheduleStart: String? = null,
        scheduleEnd: String? = null,
        scheduleNormMinutes: Int? = null
    ) {
        viewModelScope.launch {
            try {
                apiService.updatePosition(
                    deptId, 
                    posId, 
                    PositionUpdateRequest(name, hierarchyLevel, null, scheduleType, scheduleDays, scheduleStart, scheduleEnd, scheduleNormMinutes)
                )
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateWorker(userId: Int, departmentId: Int?, positionId: Int?, role: String?) {
        viewModelScope.launch {
            try {
                apiService.updateUser(userId, WorkerUpdateRequest(departmentId, positionId, role))
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun kickWorker(userId: Int) {
        viewModelScope.launch {
            try {
                apiService.deleteUserFromCompany(userId)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadWorkerStats(userId: Int) {
        viewModelScope.launch {
            try {
                _workerStats.value = apiService.getUserStats(userId)
            } catch (e: Exception) {
                e.printStackTrace()
                _workerStats.value = null
            }
        }
    }

    fun clearWorkerStats() {
        _workerStats.value = null
    }
}

class CompanyManagementViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanyManagementViewModel::class.java)) {
            return CompanyManagementViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
