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
import ru.moonlited.pocketmanager.data.api.MaslachResponse
import ru.moonlited.pocketmanager.data.api.MunsterbergResponse
import ru.moonlited.pocketmanager.utils.SessionManager
import java.time.Instant

class PomodoroViewModel(
    private val applicationContext: android.content.Context,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    var isDebugMode = sessionManager.pomodoroIsDebugMode

    val totalCycles = MutableStateFlow(sessionManager.pomodoroCycles)

    val isRunning = MutableStateFlow(false)
    val currentState = MutableStateFlow("WORK") // WORK, SHORT_BREAK, LONG_BREAK
    val currentCycle = MutableStateFlow(1)
    val timeLeft = MutableStateFlow(0)
    val totalTimeForState = MutableStateFlow(0)

    private var timerJob: Job? = null
    private var startTimeStr: String? = null

    init {
        restoreState()
    }

    fun restoreState() {
        val state = sessionManager.pomodoroCurrentState
        val cycle = sessionManager.pomodoroCurrentCycle
        val endTime = sessionManager.getPomodoroEndTime()

        currentState.value = if (state == "IDLE") "WORK" else state
        currentCycle.value = cycle

        val duration = getDurationForState(currentState.value) * 60
        totalTimeForState.value = duration

        if (endTime > 0) {
            val remainingReal = endTime - System.currentTimeMillis()
            if (remainingReal > 0) {
                isRunning.value = true
                val remainingScaled = if (isDebugMode) (remainingReal * 15 / 1000).toInt() else (remainingReal / 1000).toInt()
                timeLeft.value = remainingScaled
                startTick()
            } else {
                // Timer finished while app was closed. Save if it was WORK.
                if (currentState.value == "WORK") saveSession()
                transitionToNextState()
                startTimer() // Automatically start the next period as requested
            }
        } else {
            timeLeft.value = duration
        }
    }

    private fun getDurationForState(state: String): Int {
        return when (state) {
            "WORK" -> sessionManager.pomodoroWorkDuration
            "SHORT_BREAK" -> sessionManager.pomodoroShortBreak
            "LONG_BREAK" -> sessionManager.pomodoroLongBreak
            else -> sessionManager.pomodoroWorkDuration
        }
    }

    fun toggleTimer() {
        if (isRunning.value) pauseTimer() else startTimer()
    }

    private fun startTimer() {
        if (timeLeft.value <= 0) {
            transitionToNextState()
        }

        if (startTimeStr == null) startTimeStr = Instant.now().toString()
        isRunning.value = true

        val remainingRealMillis = if (isDebugMode) (timeLeft.value * 1000L / 15) else (timeLeft.value * 1000L)
        val endTime = System.currentTimeMillis() + remainingRealMillis

        sessionManager.savePomodoroEndTime(endTime)
        sessionManager.pomodoroCurrentState = currentState.value
        sessionManager.pomodoroCurrentCycle = currentCycle.value

        scheduleAlarm(remainingRealMillis)
        startTick()
    }

    private fun startTick() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timeLeft.value > 0) {
                delay(if (isDebugMode) 1000L / 15 else 1000L)
                timeLeft.value -= 1
            }
            isRunning.value = false
            sessionManager.savePomodoroEndTime(0)
            if (currentState.value == "WORK") {
                saveSession()
            }
            transitionToNextState()
            startTimer()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        isRunning.value = false
        sessionManager.savePomodoroEndTime(0)
        cancelAlarm()
    }

    fun resetTimer() {
        pauseTimer()
        currentState.value = "WORK"
        currentCycle.value = 1
        sessionManager.pomodoroCurrentState = "WORK"
        sessionManager.pomodoroCurrentCycle = 1
        totalCycles.value = sessionManager.pomodoroCycles

        val duration = getDurationForState("WORK") * 60
        totalTimeForState.value = duration
        timeLeft.value = duration
        startTimeStr = null
    }

    private fun transitionToNextState() {
        if (currentState.value == "WORK") {
            val cycle = currentCycle.value
            if (sessionManager.pomodoroEnableLongBreak && cycle % sessionManager.pomodoroCycles == 0) {
                currentState.value = "LONG_BREAK"
            } else {
                currentState.value = "SHORT_BREAK"
            }
        } else {
            currentState.value = "WORK"
            currentCycle.value += 1
            sessionManager.pomodoroCurrentCycle = currentCycle.value
        }
        val duration = getDurationForState(currentState.value) * 60
        totalTimeForState.value = duration
        timeLeft.value = duration
    }

    private fun scheduleAlarm(delayMillis: Long) {
        val alarmManager = applicationContext.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(applicationContext, ru.moonlited.pocketmanager.utils.PomodoroAlarmReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            applicationContext, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = System.currentTimeMillis() + delayMillis
        
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm() {
        val alarmManager = applicationContext.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(applicationContext, ru.moonlited.pocketmanager.utils.PomodoroAlarmReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            applicationContext, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun saveSession() {
        viewModelScope.launch {
            try {
                val endTime = Instant.now()
                apiService.savePomodoro(
                    PomodoroCreate(
                        startTime = startTimeStr ?: endTime.toString(),
                        endTime = endTime.toString(),
                        durationMinutes = sessionManager.pomodoroWorkDuration
                    )
                )
                startTimeStr = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SanViewModel(private val apiService: ApiService, private val sessionManager: ru.moonlited.pocketmanager.utils.SessionManager) : ViewModel() {
    val isSaved = MutableStateFlow(false)

    private val _sanHistory = MutableStateFlow<List<SanTestResponse>>(emptyList())
    val sanHistory: StateFlow<List<SanTestResponse>> = _sanHistory

    private val _maslachHistory = MutableStateFlow<List<MaslachResponse>>(emptyList())
    val maslachHistory: StateFlow<List<MaslachResponse>> = _maslachHistory

    private val _munsterbergHistory = MutableStateFlow<List<MunsterbergResponse>>(emptyList())
    val munsterbergHistory: StateFlow<List<MunsterbergResponse>> = _munsterbergHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun saveResults(s: Float, a: Float, n: Float) {
        viewModelScope.launch {
            try {
                apiService.saveSanTest(SanTestCreate(s, a, n))
                sessionManager.setTestCompletedToday("san")
                isSaved.value = true
                fetchHistory()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun saveMaslachTest(emotionalExhaustion: Float, depersonalization: Float, personalAccomplishment: Float) {
        viewModelScope.launch {
            try {
                apiService.saveMaslachTest(
                    ru.moonlited.pocketmanager.data.api.MaslachCreate(
                        emotionalExhaustion, depersonalization, personalAccomplishment
                    )
                )
                sessionManager.setTestCompletedToday("maslach")
                isSaved.value = true
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun saveMunsterbergTest(correctWords: Int, timeSpentSeconds: Int, errors: Int) {
        viewModelScope.launch {
            try {
                apiService.saveMunsterbergTest(
                    ru.moonlited.pocketmanager.data.api.MunsterbergCreate(
                        correctWords, timeSpentSeconds, errors
                    )
                )
                sessionManager.setTestCompletedToday("munsterberg")
                isSaved.value = true
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _sanHistory.value = apiService.getSanResults()
                _maslachHistory.value = apiService.getMaslachResults()
                _munsterbergHistory.value = apiService.getMunsterbergResults()
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun resetState() { isSaved.value = false }
}

class PomodoroViewModelFactory(
    private val applicationContext: android.content.Context,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            return PomodoroViewModel(applicationContext, apiService, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SanViewModelFactory(private val apiService: ApiService, private val sessionManager: ru.moonlited.pocketmanager.utils.SessionManager) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SanViewModel::class.java)) {
            return SanViewModel(apiService, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}