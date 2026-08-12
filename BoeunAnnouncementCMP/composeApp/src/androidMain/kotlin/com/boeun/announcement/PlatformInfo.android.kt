package com.boeun.announcement

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android 플랫폼의 앱 버전 정보를 가져오는 Composable 함수입니다.
 * 패키지 매니저를 사용하여 Build.gradle에 설정된 versionName을 반환합니다.
 */
@Composable
actual fun getAppVersion(): String {
    val context = LocalContext.current
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "알 수 없음"
    } catch (e: Exception) {
        "1.0"
    }
}
