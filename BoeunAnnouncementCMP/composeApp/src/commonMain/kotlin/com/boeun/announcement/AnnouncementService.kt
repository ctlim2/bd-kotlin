package com.boeun.announcement

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

/**
 * 보은군청 홈페이지의 공지사항 게시판을 스크래핑하는 서비스 클래스입니다.
 * Ksoup을 사용하여 HTML을 파싱하고 Kotlin 데이터 객체로 변환합니다.
 */
class AnnouncementService {
    // HTTP 통신을 위한 Ktor 클라이언트 설정
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // 보은군청 게시판 기본 URL
    private val baseUrl = "https://www.boeun.go.kr/www/selectBbsNttList.do"

    /**
     * 특정 페이지의 공지사항 목록을 가져옵니다.
     * 
     * @param page 요청할 페이지 번호
     * @param key 게시판 키 (기본값: 142)
     * @param bbsNo 게시판 번호 (기본값: 68)
     * @return 파싱된 Announcement 객체 리스트
     */
    suspend fun fetchAnnouncements(page: Int = 1, key: Int = 142, bbsNo: Int = 68): List<Announcement> {
        return try {
            val url = "$baseUrl?key=$key&bbsNo=$bbsNo&pageIndex=$page"
            val response = client.get(url).bodyAsText()
            
            // HTML 파싱 시작
            val doc = Ksoup.parse(response, baseUrl)
            val announcements = mutableListOf<Announcement>()
            
            // 게시판 테이블의 행(tr)들을 선택합니다.
            val rows = doc.select(".p-table tbody tr, .board-list tbody tr")
            
            for (row in rows) {
                // 데이터가 없는 행(empty)이거나 칼럼 수가 부족한 경우 건너뜁니다.
                if (row.select(".empty").isNotEmpty() || row.select("td").size < 3) continue
                
                val cols = row.select("td")
                
                // 제목 요소 추출
                val titleElement = if (cols.size >= 2) cols[1].select("a").first() else row.select(".p-subject a, .subject a").first()
                val title = titleElement?.text() ?: ""
                
                // 상세 페이지 URL 추출 및 절대 경로 변환
                val relativeUrl = titleElement?.attr("href") ?: ""
                val finalUrl = when {
                    relativeUrl.startsWith("http") -> relativeUrl
                    relativeUrl.startsWith("./") -> "https://www.boeun.go.kr/www/${relativeUrl.substring(2)}"
                    else -> "https://www.boeun.go.kr$relativeUrl"
                }
                
                // URL에서 nttNo를 추출하여 고유 ID로 사용합니다.
                val id = finalUrl.substringAfter("nttNo=", "").substringBefore("&")
                    .ifEmpty { kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString() }
                
                // 날짜, 마감일, 카테고리 정보 추출
                val date = if (cols.size >= 4) cols[3].text() else row.select(".p-date, .date").text()
                val deadline = if (cols.size >= 6) cols[5].text() else null
                val categoryName = if (cols.size >= 3) cols[2].text() else row.select(".p-dept, .dept").text()

                announcements.add(
                    Announcement(
                        id = id,
                        title = title,
                        publishDate = date,
                        deadlineDate = deadline,
                        url = finalUrl,
                        category = categoryName,
                        bbsNo = bbsNo,
                    )
                )
            }
            announcements
        } catch (e: Exception) {
            // 에러 발생 시 빈 리스트를 반환합니다.
            emptyList()
        }
    }
}
