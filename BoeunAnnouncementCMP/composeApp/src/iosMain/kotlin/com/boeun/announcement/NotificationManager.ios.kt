package com.boeun.announcement

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IosNotificationManager : NotificationManager {
    override fun showNotification(title: String, message: String) {
        // Implementation using UNUserNotificationCenter would go here
    }

    override fun scheduleSync(intervalMinutes: Int) {
        // Implementation using BGTaskScheduler would go here
    }
}

@Composable
actual fun rememberNotificationManager(): NotificationManager = remember { IosNotificationManager() }
