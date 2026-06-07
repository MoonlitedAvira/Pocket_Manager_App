// utils/SessionManager.kt
package ru.moonlited.pocketmanager.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class SessionManager(context: Context) {

    private val masterKeyAlias = MasterKey.DEFAULT_MASTER_KEY_ALIAS

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Clear corrupted preferences and Keystore alias
        try {
            context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).edit().clear().commit()
            val dir = java.io.File(context.applicationInfo.dataDir, "shared_prefs")
            val file = java.io.File(dir, "secure_prefs.xml")
            if (file.exists()) {
                file.delete()
            }
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(masterKeyAlias)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        val newMasterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            newMasterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit { putString(USER_TOKEN, token) }
        _authEvent.tryEmit(true)
    }

    fun fetchAuthToken(): String? {
        return sharedPreferences.getString(USER_TOKEN, null)
    }

    private val _authEvent = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val authEvent: SharedFlow<Boolean> = _authEvent

    fun clearToken() {
        sharedPreferences.edit { 
            remove(USER_TOKEN) 
            remove("last_sync_time")
            remove("my_id")
        }
        clearTestStates()
        _authEvent.tryEmit(false)
    }

    fun saveMyId(id: Int) {
        sharedPreferences.edit { putInt("my_id", id) }
    }

    fun getMyId(): Int {
        return sharedPreferences.getInt("my_id", -1)
    }

    fun clearTestStates() {
        sharedPreferences.edit(commit = true) {
            remove("last_test_san")
            remove("last_test_maslach")
            remove("last_test_munsterberg")
        }
    }

    fun saveLastSyncTime(time: String) {
        sharedPreferences.edit { putString("last_sync_time", time) }
    }

    fun getLastSyncTime(): String? {
        return sharedPreferences.getString("last_sync_time", null)
    }

    fun savePomodoroEndTime(timeInMillis: Long) {
        sharedPreferences.edit(commit = true) { putLong("pomodoro_end_time", timeInMillis) }
    }
    fun getPomodoroEndTime(): Long {
        return sharedPreferences.getLong("pomodoro_end_time", 0L)
    }

    var pomodoroWorkDuration: Int
        get() = sharedPreferences.getInt("pomo_work", 25)
        set(value) = sharedPreferences.edit(commit = true) { putInt("pomo_work", value) }
        
    var pomodoroShortBreak: Int
        get() = sharedPreferences.getInt("pomo_short", 5)
        set(value) = sharedPreferences.edit(commit = true) { putInt("pomo_short", value) }
        
    var pomodoroLongBreak: Int
        get() = sharedPreferences.getInt("pomo_long", 15)
        set(value) = sharedPreferences.edit(commit = true) { putInt("pomo_long", value) }
        
    var pomodoroEnableLongBreak: Boolean
        get() = sharedPreferences.getBoolean("pomo_enable_long", true)
        set(value) = sharedPreferences.edit(commit = true) { putBoolean("pomo_enable_long", value) }
        
    var pomodoroCycles: Int
        get() = sharedPreferences.getInt("pomo_cycles", 4)
        set(value) = sharedPreferences.edit(commit = true) { putInt("pomo_cycles", value) }
        
    var pomodoroCurrentState: String
        get() = sharedPreferences.getString("pomo_state", "IDLE") ?: "IDLE"
        set(value) = sharedPreferences.edit(commit = true) { putString("pomo_state", value) }
        
    var pomodoroCurrentCycle: Int
        get() = sharedPreferences.getInt("pomo_cycle", 1)
        set(value) = sharedPreferences.edit(commit = true) { putInt("pomo_cycle", value) }

    var pomodoroIsDebugMode: Boolean
        get() = sharedPreferences.getBoolean("pomo_debug", false)
        set(value) = sharedPreferences.edit(commit = true) { putBoolean("pomo_debug", value) }

    var usePomodoroMethod: Boolean
        get() = sharedPreferences.getBoolean("use_pomodoro_method", true)
        set(value) = sharedPreferences.edit(commit = true) { putBoolean("use_pomodoro_method", value) }

    fun setTestCompletedToday(testName: String) {
        val today = java.time.LocalDate.now().toString()
        sharedPreferences.edit(commit = true) { putString("last_test_$testName", today) }
    }

    fun isTestCompletedToday(testName: String): Boolean {
        val today = java.time.LocalDate.now().toString()
        val lastDate = sharedPreferences.getString("last_test_$testName", "")
        return today == lastDate
    }

    fun clearLocalTimers() {
        sharedPreferences.edit(commit = true) {
            remove("last_test_san")
            remove("last_test_maslach")
            remove("last_test_munsterberg")
        }
    }

    companion object {
        const val USER_TOKEN = "user_token"
    }
}