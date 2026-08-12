package com.boeun.announcement

import androidx.compose.runtime.Composable

interface NotificationManager {
    fun showNotification(title: String, message: String)
    fun scheduleSync(intervalMinutes: Int)
}

@Composable
expect fun rememberNotificationManager(): NotificationManager
