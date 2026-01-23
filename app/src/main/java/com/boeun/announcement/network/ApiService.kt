package com.boeun.announcement.network

import com.boeun.announcement.data.AnnouncementResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 보은군 공고 API 서비스 인터페이스
 * Retrofit을 사용하여 서버와 통신
 */
interface ApiService {
    
    /**
     * 공고 목록을 가져옵니다
     * @param page 페이지 번호 (기본값: 1)
     * @param limit 한 페이지당 항목 수 (기본값: 20)
     */
    @GET("api/announcements")
    suspend fun getAnnouncements(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<AnnouncementResponse>
    
    /**
     * 최신 공고만 가져옵니다 (백그라운드 체크용)
     */
    @GET("api/announcements/latest")
    suspend fun getLatestAnnouncements(
        @Query("limit") limit: Int = 5
    ): Response<AnnouncementResponse>
}
