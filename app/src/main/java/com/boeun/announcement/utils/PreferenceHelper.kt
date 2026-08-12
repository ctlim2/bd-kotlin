package com.boeun.announcement.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences를 쉽게 사용하기 위한 헬퍼 클래스
 */
class PreferenceHelper(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * 마지막으로 확인한 공고 ID를 저장합니다
     */
    fun saveLastCheckedAnnouncementId(id: String) {
        prefs.edit().putString(KEY_LAST_CHECKED_ID, id).apply()
    }
    
    /**
     * 마지막으로 확인한 공고 ID를 가져옵니다
     */
    fun getLastCheckedAnnouncementId(): String? {
        return prefs.getString(KEY_LAST_CHECKED_ID, null)
    }
    
    /**
     * 마지막 체크 시간을 저장합니다
     */
    fun saveLastCheckTime(timeMillis: Long) {
        prefs.edit().putLong(KEY_LAST_CHECK_TIME, timeMillis).apply()
    }
    
    /**
     * 마지막 체크 시간을 가져옵니다
     */
    fun getLastCheckTime(): Long {
        return prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
    }

    /**
     * 알림 활성화 여부를 저장합니다
     */
    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTI_ENABLED, enabled).apply()
    }

    /**
     * 알림 활성화 여부를 가져옵니다 (기본값: true)
     */
    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTI_ENABLED, true)
    }

    /**
     * 체크 주기를 저장합니다 (단위: 분)
     */
    fun setCheckInterval(minutes: Int) {
        prefs.edit().putInt(KEY_CHECK_INTERVAL, minutes).apply()
    }

    /**
     * 체크 주기를 가져옵니다 (기본값: 15분)
     */
    fun getCheckInterval(): Int {
        return prefs.getInt(KEY_CHECK_INTERVAL, 15)
    }

    companion object {
        private const val PREF_NAME = "boeun_announcement_prefs"
        private const val KEY_LAST_CHECKED_ID = "last_checked_id"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
        private const val KEY_NOTI_ENABLED = "noti_enabled"
        private const val KEY_CHECK_INTERVAL = "check_interval"
        
        @Volatile
        private var instance: PreferenceHelper? = null
        
        fun getInstance(context: Context): PreferenceHelper {
            return instance ?: synchronized(this) {
                instance ?: PreferenceHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}
