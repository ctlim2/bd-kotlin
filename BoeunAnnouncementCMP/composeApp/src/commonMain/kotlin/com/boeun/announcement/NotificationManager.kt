package com.boeun.announcement

import androidx.compose.runtime.Composable

/**
 * 플랫폼별 알림 기능을 추상화한 인터페이스입니다.
 * Android와 iOS에서 각각의 방식에 맞게 구현됩니다.
 */
interface AppNotificationManager {
    /**
     * 알림을 사용자에게 즉시 표시합니다.
     * 
     * @param title 알림 제목
     * @param message 알림 상세 내용
     * @param url 알림 클릭 시 이동할 URL (선택 사항)
     */
    fun showNotification(title: String, message: String, url: String? = null)

    /**
     * 정기적인 배경 동기화 작업을 예약합니다.
     * 
     * @param intervalMinutes 동기화 간격 (분 단위)
     */
    fun scheduleSync(intervalMinutes: Int)

    /**
     * 예약된 모든 동기화 작업을 취소합니다.
     */
    fun cancelSync()
}

/**
 * 플랫폼별 NotificationManager 구현체를 반환하는 expect 함수입니다.
 */
@Composable
expect fun rememberNotificationManager(): AppNotificationManager
