package com.boeun.announcement

import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class AndroidNotificationManager(private val context: Context) : NotificationManager {
    override fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        val channelId = "announcements"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Announcements", AndroidNotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Cannot post notification without permission on Android 13+
                return
            }
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1, notification)
    }

    override fun scheduleSync(intervalMinutes: Int) {
        // Implementation using WorkManager would go here
    }
}

@Composable
actual fun rememberNotificationManager(): NotificationManager {
    val context = LocalContext.current
    return remember(context) { AndroidNotificationManager(context) }
}
