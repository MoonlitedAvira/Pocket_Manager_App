// viewmodel/FeatureViewModels.kt
package ru.moonlited.pocketmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.PomodoroCreate
import ru.moonlited.pocketmanager.data.api.SanTestCreate
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
                        start_time = startTime.toString(),
                        end_time = endTime.toString(),
                        duration_minutes = 25
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

    fun saveResults(s: Float, a: Float, n: Float) {
        viewModelScope.launch {
            try {
                apiService.saveSanTest(SanTestCreate(s, a, n))
                isSaved.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        isSaved.value = false
    }
}

class PomodoroViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PomodoroViewModel(apiService) as T
}
class SanViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SanViewModel(apiService) as T
}