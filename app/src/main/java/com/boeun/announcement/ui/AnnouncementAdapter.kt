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
    AnnouncementDiffCallback()
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
     */
    inner class AnnouncementViewHolder(
        private val binding: ItemAnnouncementBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(announcement: Announcement) {
            binding.apply {
                // 제목 설정
                textTitle.text = announcement.title
                
                // 날짜 설정
                textDate.text = "게시일: ${announcement.publishDate}"
                
                // 카테고리 설정 (있는 경우)
                if (!announcement.category.isNullOrEmpty()) {
                    textCategory.text = announcement.category
                    textCategory.visibility = android.view.View.VISIBLE
                } else {
                    textCategory.visibility = android.view.View.GONE
                }
                
                // 클릭 이벤트: 공고 URL을 브라우저로 열기
                root.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(announcement.url))
                    it.context.startActivity(intent)
                }
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
