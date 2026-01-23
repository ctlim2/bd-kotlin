package com.boeun.announcement.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * WorkManager 작업을 관리하는 헬퍼 클래스
 */
object WorkManagerHelper {
    
    /**
     * 공고 체크 작업을 예약합니다
     * 15분마다 실행되며, 앱이 종료되어도 계속 동작합니다
     */
    fun scheduleNoticeCheck(context: Context) {
        // 제약 조건 설정: 네트워크 연결이 필요함
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // 주기적 작업 요청 생성 (15분마다)
        val noticeCheckRequest = PeriodicWorkRequestBuilder<NoticeCheckWorker>(
            15, TimeUnit.MINUTES // 주기: 15분
        )
            .setConstraints(constraints)
            .addTag(NoticeCheckWorker.WORK_NAME)
            .build()
        
        // 작업 예약
        // ExistingPeriodicWorkPolicy.KEEP: 이미 예약된 작업이 있으면 유지
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NoticeCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            noticeCheckRequest
        )
    }
    
    /**
     * 예약된 공고 체크 작업을 취소합니다
     */
    fun cancelNoticeCheck(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(NoticeCheckWorker.WORK_NAME)
    }
    
    /**
     * 즉시 공고 체크를 실행합니다 (테스트용)
     */
    fun runNoticeCheckNow(context: Context) {
        WorkManager.getInstance(context)
            .enqueue(
                PeriodicWorkRequestBuilder<NoticeCheckWorker>(
                    15, TimeUnit.MINUTES
                ).build()
            )
    }
}
