package com.boeun.announcement

import kotlinx.serialization.Serializable

/**
 * 보은군 공지사항 데이터를 나타내는 데이터 클래스입니다.
 * 웹 스크래핑을 통해 얻은 공지사항의 정보를 담고 있습니다.
 * 
 * @property id 공지사항 고유 식별자 (URL의 nttNo 파라미터 기반)
 * @property title 공지사항 제목
 * @property content 공지사항 상세 내용 (현재는 사용되지 않음)
 * @property publishDate 공지사항 등록일
 * @property deadlineDate 접수 마감일 (채용공고 등에서 사용)
 * @property url 공지사항 원문 웹 페이지 주소
 * @property category 공지 유형 (예: 총무과, 보건소 등)
 * @property bbsNo 게시판 번호 (142: 채용공고, 194: 고시공고 등)
 * @property isNew 새로운 공지인지 여부
 */
@Serializable
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
