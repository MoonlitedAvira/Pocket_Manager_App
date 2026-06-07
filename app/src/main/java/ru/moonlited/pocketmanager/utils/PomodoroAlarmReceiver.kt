package ru.moonlited.pocketmanager.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import ru.moonlited.pocketmanager.R

class PomodoroAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pomodoro_channel",
                "Таймер Pomodoro",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления таймера Pomodoro"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val sessionManager = SessionManager(context)
        val state = sessionManager.pomodoroCurrentState
        
        val title = "Pocket Manager"
        val text = when (state) {
            "WORK" -> "Время работы вышло! Пора отдохнуть."
            "SHORT_BREAK" -> "Короткий перерыв окончен. За работу!"
            "LONG_BREAK" -> "Длинный перерыв окончен. За работу!"
            else -> "Таймер завершен"
        }

        val notification = NotificationCompat.Builder(context, "pomodoro_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
