package com.boeun.announcement

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

/**
 * iOS 플랫폼의 앱 버전 정보를 가져오는 Composable 함수입니다.
 * NSBundle을 사용하여 Info.plist에 설정된 버전을 반환합니다.
 */
@Composable
actual fun getAppVersion(): String {
    return NSBundle.mainBundle.infoDictionary?["CFBundleShortVersionString"] as? String ?: "1.0"
}
