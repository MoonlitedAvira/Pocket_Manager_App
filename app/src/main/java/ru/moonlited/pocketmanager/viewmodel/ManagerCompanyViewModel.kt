package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.InvitationCreate
import ru.moonlited.pocketmanager.data.api.InvitationResponse

class ManagerCompanyViewModel(private val apiService: ApiService) : ViewModel() {

    private val _invitations = MutableStateFlow<List<InvitationResponse>>(emptyList())
    val invitations: StateFlow<List<InvitationResponse>> = _invitations

    fun generateInvite(departmentId: Int? = null, positionId: Int? = null) {
        viewModelScope.launch {
            try {
                val invite = apiService.createInvitation(InvitationCreate(departmentId, positionId))
                _invitations.value = listOf(invite) + _invitations.value
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteInvite(code: String) {
        viewModelScope.launch {
            try {
                apiService.deleteInvitation(code)
                _invitations.value = _invitations.value.filter { it.code != code }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class ManagerCompanyViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManagerCompanyViewModel::class.java)) {
            return ManagerCompanyViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
