package com.boeun.announcement

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS 플랫폼 전용 알림 관리 구현 클래스입니다.
 * (현재는 스텁(Stub) 상태이며 실제 로직은 구현되어 있지 않습니다.)
 */
class IosNotificationManager : AppNotificationManager {
    /**
     * 사용자에게 알림을 표시합니다.
     */
    override fun showNotification(title: String, message: String, url: String?) {
        // UNUserNotificationCenter를 사용한 구현이 필요합니다.
    }

    /**
     * 배경 동기화 작업을 예약합니다.
     */
    override fun scheduleSync(intervalMinutes: Int) {
        // BGTaskScheduler를 사용한 구현이 필요합니다.
    }

    /**
     * 배경 동기화 작업을 취소합니다.
     */
    override fun cancelSync() {
        // BGTask를 취소하는 로직이 필요합니다.
    }
}

/**
 * iOS 플랫폼에서 NotificationManager를 제공하는 Composable 팩토리 함수입니다.
 */
@Composable
actual fun rememberNotificationManager(): AppNotificationManager = remember { IosNotificationManager() }
