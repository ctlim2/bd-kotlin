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
    
    companion object {
        private const val PREF_NAME = "boeun_announcement_prefs"
        private const val KEY_LAST_CHECKED_ID = "last_checked_id"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
        
        @Volatile
        private var instance: PreferenceHelper? = null
        
        fun getInstance(context: Context): PreferenceHelper {
            return instance ?: synchronized(this) {
                instance ?: PreferenceHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}
