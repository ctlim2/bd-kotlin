package com.boeun.announcement.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.boeun.announcement.repository.AnnouncementRepository
import com.boeun.announcement.utils.NotificationHelper
import com.boeun.announcement.utils.PreferenceHelper

/**
 * 백그라운드에서 새로운 공고를 체크하는 Worker
 * WorkManager를 통해 15분마다 실행됩니다
 */
class NoticeCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val repository = AnnouncementRepository.getInstance()
    private val preferenceHelper = PreferenceHelper.getInstance(applicationContext)
    private val notificationHelper = NotificationHelper(applicationContext)
    
    override suspend fun doWork(): Result {
        // 알림 설정이 꺼져 있으면 작업을 건너뜀
        if (!preferenceHelper.isNotificationEnabled()) {
            Log.d(TAG, "알림 설정이 비활성화되어 작업을 중단합니다.")
            return Result.success()
        }

        Log.d(TAG, "공고 체크 시작...")
        
        return try {
            // 최신 공고를 가져옵니다
            val result = repository.getLatestAnnouncements(limit = 5)
            
            result.fold(
                onSuccess = { announcements ->
                    if (announcements.isNotEmpty()) {
                        val latestAnnouncement = announcements.first()
                        val lastCheckedId = preferenceHelper.getLastCheckedAnnouncementId()
                        
                        Log.d(TAG, "최신 공고 ID: ${latestAnnouncement.id}")
                        Log.d(TAG, "마지막 체크 ID: $lastCheckedId")
                        
                        // 새로운 공고가 있는지 확인
                        if (lastCheckedId == null || latestAnnouncement.id != lastCheckedId) {
                            // 새로운 공고 발견!
                            Log.d(TAG, "새로운 공고 발견: ${latestAnnouncement.title}")
                            
                            // 알림 표시
                            notificationHelper.showNewAnnouncementNotification(
                                title = latestAnnouncement.title,
                                date = latestAnnouncement.publishDate,
                                url = latestAnnouncement.url,
                                announcementId = latestAnnouncement.id
                            )
                            
                            // 마지막 체크 ID 업데이트
                            preferenceHelper.saveLastCheckedAnnouncementId(latestAnnouncement.id)
                        } else {
                            Log.d(TAG, "새로운 공고 없음")
                        }
                        
                        // 마지막 체크 시간 업데이트
                        preferenceHelper.saveLastCheckTime(System.currentTimeMillis())
                    }
                    
                    Result.success()
                },
                onFailure = { exception ->
                    Log.e(TAG, "공고 체크 실패: ${exception.message}", exception)
                    // 재시도
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "작업 실행 중 오류: ${e.message}", e)
            Result.failure()
        }
    }
    
    companion object {
        private const val TAG = "NoticeCheckWorker"
        const val WORK_NAME = "notice_check_work"
    }
}
