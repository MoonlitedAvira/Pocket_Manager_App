package ru.moonlited.pocketmanager.utils

import android.util.Log

object AppLogger {
    private const val TAG = "PM_DEBUG_LOG"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
