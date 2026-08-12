package com.boeun.announcement.repository

import com.boeun.announcement.data.Announcement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * 공고 데이터 저장소
 * API 호출을 관리하고 데이터를 제공
 */
class AnnouncementRepository {
    
    private val baseUrl = "https://www.boeun.go.kr/www/selectBbsNttList.do"
    
    /**
     * 공고 목록을 HTML 파싱하여 가져옵니다
     */
    suspend fun getAnnouncements(page: Int = 1, key: Int = 142, bbsNo: Int = 68): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                // 파라미터 구성
                val url = "$baseUrl?key=$key&bbsNo=$bbsNo&integrDeptCodepageIndex=&pageIndex=$page"
                
                // HTML 문서 가져오기
                val doc: Document = Jsoup.connect(url)
                    .timeout(10000)
                    .get()
                
                val announcements = mutableListOf<Announcement>()
                
                // 공고 목록 테이블의 행(tr) 선택
                // 보은군청 게시판 구조에 맞춘 셀렉터 (보통 .p-table tbody tr 또는 .board-list tbody tr)
                val rows = doc.select(".p-table tbody tr, .board-list tbody tr")
                
                for (row in rows) {
                    // 데이터가 없는 경우(검색 결과가 없습니다 등) 제외
                    if (row.select(".empty").isNotEmpty() || row.select("td").size < 3) continue
                    
                    val cols = row.select("td")
                    
                    // 제목과 상세 링크 추출 (제목은 2번째 컬럼: cols[1])
                    val titleElement = if (cols.size >= 2) cols[1].select("a").first() else row.select(".p-subject a, .subject a").first()
                    val title = titleElement?.text() ?: ""
                    
                    val fullUrl = titleElement?.absUrl("href") ?: ""
                    val finalUrl = if (fullUrl.isNotEmpty()) {
                        fullUrl
                    } else {
                        val relativeUrl = titleElement?.attr("href") ?: ""
                        when {
                            relativeUrl.startsWith("http") -> relativeUrl
                            relativeUrl.startsWith("./") -> "https://www.boeun.go.kr/www/${relativeUrl.substring(2)}"
                            else -> "https://www.boeun.go.kr$relativeUrl"
                        }
                    }
                    
                    // ID 추출
                    val id = finalUrl.substringAfter("nttNo=", "").substringBefore("&")
                        .ifEmpty { System.currentTimeMillis().toString() }
                    
                    // 날짜 추출 (작성일은 4번째 컬럼: cols[3])
                    val date = if (cols.size >= 4) {
                        cols[3].text() // 작성일
                    } else {
                        row.select(".p-date, .date").text()
                    }
                    
                    // 마감일 추출 (마감일은 6번째 컬럼: cols[5])
                    val deadline = if (cols.size >= 6) {
                        cols[5].text() // 마감일
                    } else {
                        null
                    }
                    
                    // 작성자/부서 추출 (작성자는 3번째 컬럼: cols[2])
                    val category = if (cols.size >= 3) {
                        cols[2].text() // 작성자
                    } else {
                        row.select(".p-dept, .dept").text()
                    }

                    announcements.add(
                        Announcement(
                            id = id,
                            title = title,
                            publishDate = date,
                            deadlineDate = deadline,
                            url = finalUrl,
                            category = category,
                            bbsNo = bbsNo,
                        )
                    )
                }
                
                Result.success(announcements)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 최신 공고를 가져옵니다 (백그라운드 체크용)
     */
    suspend fun getLatestAnnouncements(limit: Int = 5): Result<List<Announcement>> {
        val result = getAnnouncements(1)
        return result.map { it.take(limit) }
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
