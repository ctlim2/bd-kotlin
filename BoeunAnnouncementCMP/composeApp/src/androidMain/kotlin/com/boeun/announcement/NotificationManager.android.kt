package com.boeun.announcement

import android.content.Context
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.app.NotificationManager as AndroidNotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Android 플랫폼 전용 알림 관리 구현 클래스입니다.
 * 시스템 알림 채널 생성, 알림 표시, WorkManager를 이용한 동기화 예약을 담당합니다.
 */
class BoeunNotificationManager(private val context: Context) : AppNotificationManager {
    
    /**
     * 사용자에게 상단바 알림을 표시합니다.
     * 
     * @param title 알림 제목
     * @param message 알림 내용 (공지사항 제목)
     * @param url 알림 클릭 시 이동할 웹 주소
     */
    override fun showNotification(title: String, message: String, url: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        val channelId = "announcements"
        
        // Android 8.0(Oreo) 이상에서는 알림 채널이 필수입니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Announcements", AndroidNotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Android 13 이상에서 알림 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        /**
         * 알림 클릭 처리 로직:
         * 1. URL이 전달된 경우 시스템 브라우저를 통해 해당 웹페이지를 엽니다.
         * 2. URL이 없는 경우 앱의 메인 화면을 실행합니다.
         */
        val intent = if (!url.isNullOrEmpty()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent) // 클릭 시 실행할 동작 설정
            .setAutoCancel(true) // 클릭 시 알림 자동 삭제
            .build()
            
        // URL의 해시코드를 ID로 사용하여 고유한 알림을 생성합니다. (중복 방지)
        notificationManager.notify(url?.hashCode() ?: 1, notification)
    }

    /**
     * WorkManager를 사용하여 배경 동기화 작업을 예약합니다.
     * 
     * @param intervalMinutes 동기화 주기 (분 단위)
     */
    override fun scheduleSync(intervalMinutes: Int) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED) // 네트워크 연결 시에만 실행
            .build()

        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<AnnouncementWorker>(
            intervalMinutes.toLong(), java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // 동일한 이름의 기존 작업을 업데이트하여 중복 등록을 방지합니다.
        workManager.enqueueUniquePeriodicWork(
            "announcement_sync",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    /**
     * 예약된 모든 배경 동기화 작업을 취소합니다.
     */
    override fun cancelSync() {
        val workManager = androidx.work.WorkManager.getInstance(context)
        workManager.cancelUniqueWork("announcement_sync")
    }
}

/**
 * Android 플랫폼에서 NotificationManager를 제공하는 Composable 팩토리 함수입니다.
 */
@Composable
actual fun rememberNotificationManager(): AppNotificationManager {
    val context = LocalContext.current
    return remember(context) { BoeunNotificationManager(context) }
}
