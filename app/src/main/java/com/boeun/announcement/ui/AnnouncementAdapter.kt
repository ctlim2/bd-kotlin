package com.boeun.announcement.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boeun.announcement.data.Announcement
import com.boeun.announcement.databinding.ItemAnnouncementBinding

/**
 * 공고 목록을 표시하는 RecyclerView Adapter
 * ListAdapter를 사용하여 효율적인 업데이트 지원
 */
class AnnouncementAdapter : ListAdapter<Announcement, AnnouncementAdapter.AnnouncementViewHolder>(
    AnnouncementDiffCallback(),
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val binding = ItemAnnouncementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnnouncementViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * ViewHolder 클래스
     * 외부 클래스 참조가 필요 없으므로 static 클래스(inner 미사용)로 선언하여 메모리 효율성 향상
     */
    class AnnouncementViewHolder(
        private val binding: ItemAnnouncementBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(announcement: Announcement) {
            binding.apply {
                // 제목 설정
                textTitle.text = announcement.title
                
                // 날짜 설정
                textDate.text = "게시일: ${announcement.publishDate}"
                
                // 마감일 설정 및 마감 여부 확인
                // 고시/공고(bbsNo=66)의 경우 마감일을 표시하지 않음
                if (!announcement.deadlineDate.isNullOrEmpty() && announcement.bbsNo != 66) {
                    textDeadline.text = "마감일: ${announcement.deadlineDate}"
                    textDeadline.visibility = android.view.View.VISIBLE
                    
                    // 현재 날짜와 비교하여 마감 여부 판단
                    val isPast = isDeadlinePast(announcement.deadlineDate)
                    if (isPast) {
                        textDeadline.text = "마감 완료 (${announcement.deadlineDate})"
                        root.alpha = 0.5f // 흐리게 표시
                    } else {
                        root.alpha = 1.0f // 정상 표시
                    }
                } else {
                    textDeadline.visibility = android.view.View.GONE
                    root.alpha = 1.0f
                }
                
                // 카테고리 설정 (있는 경우)
                if (!announcement.category.isNullOrEmpty()) {
                    textCategory.text = announcement.category
                    textCategory.visibility = android.view.View.VISIBLE
                } else {
                    textCategory.visibility = android.view.View.GONE
                }
                
                // 클릭 이벤트: 공고 URL을 브라우저로 열기
                root.setOnClickListener {
                    try {
                        val url = announcement.url
                        if (url.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            it.context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        // 유효하지 않은 URL이거나 브라우저가 없는 경우 예외 처리
                        e.printStackTrace()
                    }
                }
            }
        }
        
        /**
         * 마감일이 현재 날짜보다 지났는지 확인
         */
        private fun isDeadlinePast(deadline: String): Boolean {
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val deadlineDate = sdf.parse(deadline)
                val currentDate = java.util.Date()
                // 오늘 날짜(자정 기준)와 비교하기 위해 시간 정보 제거
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                
                deadlineDate != null && deadlineDate.before(calendar.time)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * DiffUtil 콜백: 효율적인 리스트 업데이트를 위한 비교 로직
     */
    private class AnnouncementDiffCallback : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
            return oldItem == newItem
        }
    }
}
