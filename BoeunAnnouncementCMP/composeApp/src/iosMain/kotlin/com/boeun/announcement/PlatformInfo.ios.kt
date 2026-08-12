package com.boeun.announcement

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

actual val appVersion: String
    @Composable
    get() {
        return NSBundle.mainBundle.infoDictionary?["CFBundleShortVersionString"] as? String ?: "1.0"
    }
