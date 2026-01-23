package com.boeun.announcement.repository

import com.boeun.announcement.data.Announcement
import com.boeun.announcement.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 공고 데이터 저장소
 * API 호출을 관리하고 데이터를 제공
 */
class AnnouncementRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * 공고 목록을 가져옵니다
     */
    suspend fun getAnnouncements(page: Int = 1): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAnnouncements(page)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()?.data ?: emptyList())
                } else {
                    Result.failure(Exception("데이터를 가져오는데 실패했습니다: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 최신 공고를 가져옵니다 (백그라운드 체크용)
     */
    suspend fun getLatestAnnouncements(limit: Int = 5): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLatestAnnouncements(limit)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()?.data ?: emptyList())
                } else {
                    Result.failure(Exception("최신 공고를 가져오는데 실패했습니다"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    companion object {
        @Volatile
        private var instance: AnnouncementRepository? = null
        
        fun getInstance(): AnnouncementRepository {
            return instance ?: synchronized(this) {
                instance ?: AnnouncementRepository().also { instance = it }
            }
        }
    }
}
