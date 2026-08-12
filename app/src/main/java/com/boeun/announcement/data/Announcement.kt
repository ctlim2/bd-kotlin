package com.boeun.announcement.data

/**
 * 보은군 공고 데이터 클래스
 * HTML 파싱을 통해 추출한 공고 정보를 담는 모델
 */
data class Announcement(
    val id: String,
    val title: String,
    val content: String? = null,
    val publishDate: String,
    val deadlineDate: String? = null,
    val url: String,
    val category: String? = null,
    val bbsNo: Int = 68,
    val isNew: Boolean = false
)
