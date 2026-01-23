package com.boeun.announcement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boeun.announcement.data.Announcement
import com.boeun.announcement.repository.AnnouncementRepository
import kotlinx.coroutines.launch

/**
 * 공고 목록 화면의 ViewModel
 * UI와 데이터 계층 사이의 중개자 역할
 */
class AnnouncementViewModel(
    private val repository: AnnouncementRepository = AnnouncementRepository.getInstance()
) : ViewModel() {
    
    // 공고 목록
    private val _announcements = MutableLiveData<List<Announcement>>()
    val announcements: LiveData<List<Announcement>> = _announcements
    
    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 에러 메시지
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadAnnouncements()
    }
    
    /**
     * 공고 목록을 불러옵니다
     */
    fun loadAnnouncements(page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getAnnouncements(page)
            result.fold(
                onSuccess = { announcements ->
                    _announcements.value = announcements
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "알 수 없는 오류가 발생했습니다"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    /**
     * 새로고침
     */
    fun refresh() {
        loadAnnouncements()
    }
}
