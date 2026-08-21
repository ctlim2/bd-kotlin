package com.boeun.announcement

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

class AnnouncementService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val baseUrl = "https://www.boeun.go.kr/www/selectBbsNttList.do"

    suspend fun fetchAnnouncements(page: Int = 1, key: Int = 142, bbsNo: Int = 68): List<Announcement> {
        return try {
            val url = "$baseUrl?key=$key&bbsNo=$bbsNo&pageIndex=$page"
            val response = client.get(url).bodyAsText()
            val doc = Ksoup.parse(response, baseUrl)
            
            val announcements = mutableListOf<Announcement>()
            val rows = doc.select(".p-table tbody tr, .board-list tbody tr")
            
            for (row in rows) {
                if (row.select(".empty").isNotEmpty() || row.select("td").size < 3) continue
                
                val cols = row.select("td")
                val titleElement = if (cols.size >= 2) cols[1].select("a").first() else row.select(".p-subject a, .subject a").first()
                val title = titleElement?.text() ?: ""
                
                val relativeUrl = titleElement?.attr("href") ?: ""
                val finalUrl = when {
                    relativeUrl.startsWith("http") -> relativeUrl
                    relativeUrl.startsWith("./") -> "https://www.boeun.go.kr/www/${relativeUrl.substring(2)}"
                    else -> "https://www.boeun.go.kr$relativeUrl"
                }
                
                val id = finalUrl.substringAfter("nttNo=", "").substringBefore("&")
                    .ifEmpty { kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString() }
                
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
            emptyList()
        }
    }
}
