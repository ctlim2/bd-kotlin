package com.boeun.announcement.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.boeun.announcement.databinding.ActivityMainBinding
import com.boeun.announcement.viewmodel.AnnouncementViewModel
import com.boeun.announcement.worker.WorkManagerHelper

/**
 * 메인 액티비티
 * 공고 목록을 표시하고 백그라운드 작업을 시작합니다
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: AnnouncementViewModel by viewModels()
    private lateinit var adapter: AnnouncementAdapter
    
    // 알림 권한 요청 런처 (Android 13 이상)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "알림 권한이 허용되었습니다", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "알림 권한이 거부되었습니다. 새 공고 알림을 받을 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners() // 버튼 클릭 리스너 설정 추가
        setupTabSelection()   // 탭 선택 설정 추가
        observeViewModel()
        
        // 알림 권한 확인 및 요청 (Android 13 이상)
        checkNotificationPermission()
        
        // 백그라운드 작업 예약
        WorkManagerHelper.scheduleNoticeCheck(this)
    }
    
    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        adapter = AnnouncementAdapter()
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }
    
    /**
     * SwipeRefreshLayout 설정
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }
    
    /**
     * 버튼 클릭 리스너 설정
     */
    private fun setupClickListeners() {
        binding.fabRefresh.setOnClickListener {
            viewModel.refresh()
            android.widget.Toast.makeText(this, "공고를 새로 불러옵니다", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // 이전 페이지 버튼
        binding.btnPrev.setOnClickListener {
            viewModel.prevPage()
        }
        
        // 다음 페이지 버튼
        binding.btnNext.setOnClickListener {
            viewModel.nextPage()
        }
        
        // 설정 버튼 클릭
        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    /**
     * 세련된 설정 다이얼로그 표시
     */
    private fun showSettingsDialog() {
        val preferenceHelper = com.boeun.announcement.utils.PreferenceHelper.getInstance(this)
        val intervals = arrayOf("15분", "30분", "1시간", "3시간", "하루")
        val intervalValues = arrayOf(15, 30, 60, 180, 1440)
        
        val currentIntervalIndex = intervalValues.indexOf(preferenceHelper.getCheckInterval()).let { if (it == -1) 0 else it }
        
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        // 알림 설정 항목
        val switchNoti = com.google.android.material.switchmaterial.SwitchMaterial(this).apply {
            text = "새 공고 푸시 알림"
            isChecked = preferenceHelper.isNotificationEnabled()
            textSize = 16f
            setPadding(0, 20, 0, 40)
        }
        container.addView(switchNoti)

        // 주기 설정 항목 (텍스트 클릭형)
        val intervalTitle = android.widget.TextView(this).apply {
            text = "체크 주기 설정"
            textSize = 16f
            setTextColor(android.graphics.Color.BLACK)
        }
        val intervalValueText = android.widget.TextView(this).apply {
            text = intervals[currentIntervalIndex]
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, com.boeun.announcement.R.color.purple_500))
            setPadding(0, 10, 0, 20)
        }
        
        val intervalLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            setOnClickListener {
                // 현재 설정된 주기를 다시 가져와서 선택 상태로 만듦
                val latestInterval = preferenceHelper.getCheckInterval()
                val latestIndex = intervalValues.indexOf(latestInterval).let { if (it == -1) 0 else it }
                
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("주기 선택")
                    .setSingleChoiceItems(intervals, latestIndex) { d, which ->
                        preferenceHelper.setCheckInterval(intervalValues[which])
                        intervalValueText.text = intervals[which]
                        WorkManagerHelper.scheduleNoticeCheck(this@MainActivity)
                        d.dismiss()
                    }
                    .show()
            }
            addView(intervalTitle)
            addView(intervalValueText)
        }
        container.addView(intervalLayout)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("환경 설정")
            .setView(container)
            .setPositiveButton("확인") { _, _ ->
                preferenceHelper.setNotificationEnabled(switchNoti.isChecked)
                WorkManagerHelper.scheduleNoticeCheck(this)
                Toast.makeText(this, "설정이 저장되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    /**
     * 탭 선택 설정 (일반공고 vs 고시/공고)
     */
    private fun setupTabSelection() {
        binding.tabCategory.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setCategory(142, 68) // 채용공고
                    1 -> viewModel.setCategory(194, 66) // 고시/공고
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }
    
    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 공고 목록 관찰
        viewModel.announcements.observe(this) { announcements ->
            adapter.submitList(announcements)
        }
        
        // 로딩 상태 관찰
        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
        
        // 에러 메시지 관찰
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
        
        // 현재 페이지 번호 관찰
        viewModel.currentPage.observe(this) { page ->
            // 1페이지에서는 이전 버튼 비활성화
            binding.btnPrev.isEnabled = page > 1
        }
        
        // 페이지 번호 목록 관찰 및 버튼 생성
        viewModel.pageList.observe(this) { pages ->
            renderPageButtons(pages, viewModel.currentPage.value ?: 1)
        }
    }
    
    /**
     * 페이지 번호 버튼들을 동적으로 생성하여 레이아웃에 추가합니다.
     */
    private fun renderPageButtons(pages: List<Int>, currentPage: Int) {
        binding.layoutPageNumbers.removeAllViews()
        
        for (page in pages) {
            val button = android.widget.Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
                text = page.toString()
                minWidth = 0
                minimumWidth = 0
                setPadding(20, 0, 20, 0)
                
                // 현재 페이지 강조
                if (page == currentPage) {
                    setBackgroundColor(ContextCompat.getColor(context, com.boeun.announcement.R.color.purple_500))
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                } else {
                    setTextColor(ContextCompat.getColor(context, com.boeun.announcement.R.color.purple_500))
                }
                
                setOnClickListener {
                    viewModel.loadAnnouncements(page)
                }
            }
            binding.layoutPageNumbers.addView(button)
        }
    }
    
    /**
     * 알림 권한 확인 및 요청 (Android 13 이상)
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 이미 허용됨
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 권한 설명이 필요한 경우
                    Toast.makeText(
                        this,
                        "새로운 공고 알림을 받으려면 알림 권한이 필요합니다",
                        Toast.LENGTH_LONG
                    ).show()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // 권한 요청
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
