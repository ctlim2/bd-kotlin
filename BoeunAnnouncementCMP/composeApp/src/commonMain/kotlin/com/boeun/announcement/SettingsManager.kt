package com.boeun.announcement

import com.russhwolf.settings.Settings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock

/**
 * 애플리케이션의 설정 및 상태 데이터를 관리하는 클래스입니다.
 * Multiplatform Settings 라이브러리를 사용하여 로컬 저장소에 데이터를 읽고 씁니다.
 */
class SettingsManager(private val settings: Settings) {
    /**
     * 알림 활성화 여부 설정
     * 기본값: true
     */
    var notificationsEnabled: Boolean
        get() = settings.getBoolean("notifications_enabled", true)
        set(value) = settings.putBoolean("notifications_enabled", value)

    /**
     * 배경 동기화 주기 (분 단위)
     * 기본값: 60분
     */
    var syncIntervalMinutes: Int
        get() = settings.getInt("sync_interval", 60)
        set(value) = settings.putInt("sync_interval", value)

    /**
     * 마지막으로 확인한 공지사항의 고유 ID
     * 새로운 공지사항 발생 여부를 판단하는 데 사용됩니다.
     */
    var lastSeenId: String
        get() = settings.getString("last_seen_id", "")
        set(value) = settings.putString("last_seen_id", value)

    /**
     * 동기화 수행 로그를 추가합니다.
     * 로그는 최신순으로 정렬되며 최대 50개까지 보관합니다.
     * 
     * @param message 기록할 로그 메시지
     */
    fun addSyncLog(message: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timestamp = "${now.monthNumber}/${now.dayOfMonth} ${now.hour}:${now.minute}"
        val newLog = "[$timestamp] $message"
        val currentLogs = getSyncLogs().toMutableList()
        currentLogs.add(0, newLog)
        
        // 로그 개수를 50개로 제한하여 저장 공간 낭비를 방지합니다.
        if (currentLogs.size > 50) currentLogs.removeAt(currentLogs.size - 1)
        settings.putString("sync_logs", currentLogs.joinToString("\n"))
    }

    /**
     * 저장된 모든 동기화 로그 목록을 가져옵니다.
     * 관리자 모드에서 확인 가능합니다.
     */
    fun getSyncLogs(): List<String> {
        val logs = settings.getString("sync_logs", "")
        return if (logs.isEmpty()) emptyList() else logs.split("\n")
    }
}
