// viewmodel/FeatureViewModels.kt
package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.PomodoroCreate
import ru.moonlited.pocketmanager.data.api.SanTestCreate
import ru.moonlited.pocketmanager.data.api.SanTestResponse
import java.time.Instant

class PomodoroViewModel(private val apiService: ApiService) : ViewModel() {
    private val totalTime = 25 * 60

    val timeLeft = MutableStateFlow(totalTime)
    val isRunning = MutableStateFlow(false)

    private var timerJob: Job? = null
    private var startTime: Instant? = null

    fun toggleTimer() {
        if (isRunning.value) {
            timerJob?.cancel()
            isRunning.value = false
        } else {
            if (startTime == null) startTime = Instant.now()
            isRunning.value = true
            timerJob = viewModelScope.launch {
                while (timeLeft.value > 0) {
                    delay(1000)
                    timeLeft.value -= 1
                }
                isRunning.value = false
                saveSession()
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        isRunning.value = false
        timeLeft.value = totalTime
        startTime = null
    }

    private fun saveSession() {
        viewModelScope.launch {
            try {
                val endTime = Instant.now()
                apiService.savePomodoro(
                    PomodoroCreate(
                        startTime = startTime.toString(),
                        endTime = endTime.toString(),
                        durationMinutes = 25
                    )
                )
                startTime = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SanViewModel(private val apiService: ApiService) : ViewModel() {
    val isSaved = MutableStateFlow(false)

    private val _sanHistory = MutableStateFlow<List<SanTestResponse>>(emptyList())
    val sanHistory: StateFlow<List<SanTestResponse>> = _sanHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun saveResults(s: Float, a: Float, n: Float) {
        viewModelScope.launch {
            try {
                apiService.saveSanTest(SanTestCreate(s, a, n))
                isSaved.value = true
                fetchHistory()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _sanHistory.value = apiService.getSanResults()
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun resetState() { isSaved.value = false }
}

class PomodoroViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            return PomodoroViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SanViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SanViewModel::class.java)) {
            return SanViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}