package com.boeun.announcement

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual val appVersion: String
    @Composable
    get() {
        val context = LocalContext.current
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "알 수 없음"
        } catch (e: Exception) {
            "1.0"
        }
    }
