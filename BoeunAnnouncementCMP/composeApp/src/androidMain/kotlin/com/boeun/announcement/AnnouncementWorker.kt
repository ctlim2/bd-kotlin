package com.boeun.announcement

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 배경에서 정기적으로 새로운 공지사항을 확인하는 워커 클래스입니다.
 * Android WorkManager에 의해 실행되며, 새로운 공지가 발견되면 알림을 생성합니다.
 */
class AnnouncementWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * 배경 작업의 실제 로직이 실행되는 메소드입니다.
     * 공지사항을 가져와서 마지막으로 확인한 ID와 비교합니다.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val settings = Settings()
        val settingsManager = SettingsManager(settings)
        
        // 사용자가 알림 설정을 끈 경우 작업을 조기에 종료합니다.
        if (!settingsManager.notificationsEnabled) {
            return@withContext Result.success()
        }

        val service = AnnouncementService()
        try {
            // 첫 페이지의 공지사항 목록을 가져옵니다.
            val announcements = service.fetchAnnouncements(page = 1)
            
            if (announcements.isNotEmpty()) {
                val latest = announcements.first()
                val lastId = settingsManager.lastSeenId
                
                // 마지막으로 확인한 공지 ID와 최신 공지 ID가 다른 경우 새로운 공지로 판단합니다.
                if (lastId.isNotEmpty() && latest.id != lastId) {
                    val notificationManager = BoeunNotificationManager(applicationContext)
                    
                    // 알림 클릭 시 해당 공지사항의 URL로 이동하도록 설정합니다.
                    notificationManager.showNotification(
                        title = "새로운 소식이 있습니다",
                        message = latest.title,
                        url = latest.url
                    )
                    settingsManager.addSyncLog("새 공고 발견: ${latest.title}")
                } else {
                    settingsManager.addSyncLog("동기화 완료: 새로운 공고 없음")
                }
                
                // 최신 공지 ID를 저장하여 다음 확인 시 중복 알림을 방지합니다.
                settingsManager.lastSeenId = latest.id
            } else {
                settingsManager.addSyncLog("동기화 완료: 데이터가 비어있음")
            }
        } catch (e: Exception) {
            // 동기화 실패 시 관리자 로그에 기록합니다.
            settingsManager.addSyncLog("동기화 실패: ${e.message}")
        }

        Result.success()
    }
}
