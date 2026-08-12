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
    
    // 현재 페이지 번호 관리
    private val _currentPage = MutableLiveData<Int>(1)
    val currentPage: LiveData<Int> = _currentPage
    
    // 보여줄 페이지 번호 목록
    private val _pageList = MutableLiveData<List<Int>>()
    val pageList: LiveData<List<Int>> = _pageList
    
    // 현재 선택된 게시판 타입 (key, bbsNo)
    private var currentKey = 142
    private var currentBbsNo = 68
    
    init {
        loadAnnouncements(1)
    }
    
    private var loadingJob: kotlinx.coroutines.Job? = null

    /**
     * 공고 목록을 불러옵니다
     */
    fun loadAnnouncements(page: Int) {
        _currentPage.value = page
        updatePageList(page) // 페이지 목록 갱신
        
        // 이미 로딩 중인 경우 이전 작업 취소
        loadingJob?.cancel()
        
        loadingJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getAnnouncements(page, currentKey, currentBbsNo)
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
     * 게시판 카테고리 변경
     */
    fun setCategory(key: Int, bbsNo: Int) {
        currentKey = key
        currentBbsNo = bbsNo
        loadAnnouncements(1) // 첫 페이지부터 로드
    }
    
    /**
     * 다음 페이지로 이동
     */
    fun nextPage() {
        val next = (_currentPage.value ?: 1) + 1
        loadAnnouncements(next)
    }
    
    /**
     * 이전 페이지로 이동
     */
    fun prevPage() {
        val current = _currentPage.value ?: 1
        if (current > 1) {
            loadAnnouncements(current - 1)
        }
    }
    
    /**
     * 보여줄 페이지 번호 목록을 계산합니다 (예: 현재 3 -> 1, 2, 3, 4, 5)
     */
    private fun updatePageList(current: Int) {
        val start = (current - 2).coerceAtLeast(1)
        val end = start + 4 // 5개씩 보여줌
        _pageList.value = (start..end).toList()
    }
    
    /**
     * 새로고침
     */
    fun refresh() {
        loadAnnouncements(_currentPage.value ?: 1)
    }
}
