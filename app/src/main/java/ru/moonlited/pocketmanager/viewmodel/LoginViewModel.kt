// viewmodel/LoginViewModel.kt
package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.UserCreateRequest
import ru.moonlited.pocketmanager.utils.SessionManager

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
    data class AccountDeleted(val email: String) : LoginState()
}

class LoginViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = apiService.login(email, password)
                sessionManager.saveAuthToken(response.accessToken)
                _loginState.value = LoginState.Success
            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    401 -> _loginState.value = LoginState.Error("Неверный логин или пароль")
                    403 -> _loginState.value = LoginState.AccountDeleted(email)
                    else -> _loginState.value = LoginState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: java.net.ConnectException) {
                _loginState.value = LoginState.Error("Не удалось подключиться к серверу")
            } catch (e: java.net.UnknownHostException) {
                _loginState.value = LoginState.Error("Не удалось подключиться к серверу")
            } catch (e: java.net.SocketTimeoutException) {
                _loginState.value = LoginState.Error("Превышено время ожидания ответа сервера")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Ошибка авторизации: ${e.localizedMessage}")
            }
        }
    }

    fun recoverAccount(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = apiService.recoverAccount(UserCreateRequest(email, password))
                sessionManager.saveAuthToken(response.accessToken)
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Ошибка восстановления: Неверный пароль или аккаунт не удален")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                apiService.register(UserCreateRequest(email, password))
                val response = apiService.login(email, password)
                sessionManager.saveAuthToken(response.accessToken)
                _loginState.value = LoginState.Success
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 400 || e.code() == 409) {
                    _loginState.value = LoginState.Error("Email уже занят или некорректен")
                } else {
                    _loginState.value = LoginState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: java.net.ConnectException) {
                _loginState.value = LoginState.Error("Не удалось подключиться к серверу")
            } catch (e: java.net.UnknownHostException) {
                _loginState.value = LoginState.Error("Не удалось подключиться к серверу")
            } catch (e: java.net.SocketTimeoutException) {
                _loginState.value = LoginState.Error("Превышено время ожидания ответа сервера")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Неизвестная ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun createCompany(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                apiService.createCompany(ru.moonlited.pocketmanager.data.api.CompanyCreateRequest(name))
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Ошибка создания компании: ${e.localizedMessage}")
            }
        }
    }

    fun joinCompany(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                apiService.joinCompany(ru.moonlited.pocketmanager.data.api.JoinCompanyRequest(code))
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Ошибка при вступлении: ${e.localizedMessage}")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                apiService.deleteAccount()
                sessionManager.saveAuthToken("")
                _loginState.value = LoginState.AccountDeleted("")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Ошибка удаления аккаунта: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
class LoginViewModelFactory(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(apiService, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}