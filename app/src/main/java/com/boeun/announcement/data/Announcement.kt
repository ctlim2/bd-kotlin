package com.boeun.announcement.data

import com.google.gson.annotations.SerializedName

/**
 * 보은군 공고 데이터 클래스
 * 서버로부터 받아오는 공고 정보를 담는 모델
 */
data class Announcement(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("content")
    val content: String? = null,
    
    @SerializedName("publishDate")
    val publishDate: String,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("isNew")
    val isNew: Boolean = false
)

/**
 * API 응답 래퍼 클래스
 */
data class AnnouncementResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<Announcement>,
    
    @SerializedName("message")
    val message: String? = null
)
