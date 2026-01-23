package com.boeun.announcement.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.boeun.announcement.R

/**
 * 푸시 알림을 관리하는 헬퍼 클래스
 * 새로운 공고가 발견되면 상단바에 알림을 표시합니다
 */
class NotificationHelper(private val context: Context) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannel()
    }
    
    /**
     * 알림 채널 생성 (Android 8.0 이상)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 중요도: 높음
            ).apply {
                description = "보은군 신규 공고 알림을 받습니다"
                enableVibration(true)
                enableLights(true)
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 새 공고 알림을 표시합니다
     * 
     * @param title 공고 제목
     * @param date 게시 날짜
     * @param url 공고 상세 URL
     * @param announcementId 공고 ID
     */
    fun showNewAnnouncementNotification(
        title: String,
        date: String,
        url: String,
        announcementId: String
    ) {
        // URL을 열기 위한 Intent
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        // PendingIntent 생성 (알림 클릭 시 실행)
        val pendingIntent = PendingIntent.getActivity(
            context,
            announcementId.hashCode(), // 각 알림마다 고유한 ID
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 알림 빌더
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: 앱 아이콘으로 변경
            .setContentTitle("🔔 새로운 공고")
            .setContentText(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$title\n\n게시일: $date")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // 클릭 시 알림 자동 제거
            .setContentIntent(pendingIntent) // 클릭 시 URL로 이동
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        // 알림 표시
        try {
            notificationManager.notify(
                announcementId.hashCode(), // 각 공고마다 고유한 알림 ID
                notification
            )
        } catch (e: SecurityException) {
            // Android 13 이상에서 알림 권한이 없는 경우
            e.printStackTrace()
        }
    }
    
    /**
     * 특정 알림을 제거합니다
     */
    fun cancelNotification(announcementId: String) {
        notificationManager.cancel(announcementId.hashCode())
    }
    
    /**
     * 모든 알림을 제거합니다
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    companion object {
        private const val CHANNEL_ID = "boeun_announcement_channel"
        private const val CHANNEL_NAME = "보은군 공고 알림"
    }
}
