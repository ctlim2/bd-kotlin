package com.boeun.announcement

import com.russhwolf.settings.Settings

class SettingsManager(private val settings: Settings) {
    var notificationsEnabled: Boolean
        get() = settings.getBoolean("notifications_enabled", true)
        set(value) = settings.putBoolean("notifications_enabled", value)

    var syncIntervalMinutes: Int
        get() = settings.getInt("sync_interval", 60)
        set(value) = settings.putInt("sync_interval", value)
}
