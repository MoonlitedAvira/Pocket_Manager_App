package ru.moonlited.pocketmanager.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.moonlited.pocketmanager.data.api.ApiService
import ru.moonlited.pocketmanager.data.api.FCMTokenUpdate

class MyFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val apiService: ApiService by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.updateFcmToken(FCMTokenUpdate(token))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val syncRepository: ru.moonlited.pocketmanager.data.repository.SyncRepository by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val action = remoteMessage.data["action"]

        if (action == "reset_tests") {
            val sessionManager = SessionManager(applicationContext)
            sessionManager.clearLocalTimers()
            
            // Запускаем полную синхронизацию, так как тесты могли обновиться
            CoroutineScope(Dispatchers.IO).launch {
                syncRepository.syncAll()
            }
            
            val intent = android.content.Intent("ru.moonlited.pocketmanager.TASKS_UPDATED")
            intent.setPackage(packageName)
            sendBroadcast(intent)
        } else {
            // Рассылаем броадкаст для обновления задач
            CoroutineScope(Dispatchers.IO).launch {
                syncRepository.syncAll()
            }
            val intent = android.content.Intent("ru.moonlited.pocketmanager.TASKS_UPDATED")
            intent.setPackage(packageName)
            sendBroadcast(intent)
        }

        // Если это silent-push с data payload и без блока notification - не показываем системное уведомление
        if (remoteMessage.notification == null) {
            return
        }

        val title = remoteMessage.notification?.title ?: "Новое уведомление"
        val body = remoteMessage.notification?.body ?: ""

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fcm_default_channel"

        val channel = NotificationChannel(
            channelId,
            "Уведомления PocketManager",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
