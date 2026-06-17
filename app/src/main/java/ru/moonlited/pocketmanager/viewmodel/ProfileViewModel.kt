package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.AttendanceResponse
import ru.moonlited.pocketmanager.data.api.UserResponse
import ru.moonlited.pocketmanager.utils.SessionManager

class ProfileViewModel(private val apiService: ApiService, private val sessionManager: SessionManager) : ViewModel() {

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user

    private val _position = MutableStateFlow<ru.moonlited.pocketmanager.data.api.PositionResponse?>(null)
    val position: StateFlow<ru.moonlited.pocketmanager.data.api.PositionResponse?> = _position

    private val _attendances = MutableStateFlow<List<AttendanceResponse>>(emptyList())
    val attendances: StateFlow<List<AttendanceResponse>> = _attendances

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val me = apiService.getMe()
                _user.value = me
                sessionManager.saveMyId(me.id)
                try {
                    _position.value = apiService.getMyPosition()
                } catch (e: Exception) {
                    _position.value = null
                }
                _attendances.value = apiService.getAttendance()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkIn(actionType: String) {
        viewModelScope.launch {
            try {
                apiService.checkIn(ru.moonlited.pocketmanager.data.api.AttendanceCreate(actionType))
                fetchProfile() // Refresh attendances
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class ProfileViewModelFactory(private val apiService: ApiService, private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(apiService, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
