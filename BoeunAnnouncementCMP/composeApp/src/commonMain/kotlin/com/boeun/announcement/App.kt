package com.boeun.announcement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.boeun.announcement.ui.BoeunSplashScreen

/**
 * 애플리케이션의 최상위 컴포저블 함수입니다.
 * 상태 관리와 플랫폼 공통 UI 레이아웃을 정의합니다.
 */
@Composable
fun App(
    service: AnnouncementService = remember { AnnouncementService() },
    settings: com.russhwolf.settings.Settings = remember { com.russhwolf.settings.Settings() }
) {
    var showSplash by remember { mutableStateOf(true) }
    
    // 앱 시작 시 2초 동안 스플래시 화면을 표시합니다.
    if (showSplash) {
        BoeunSplashScreen()
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSplash = false
        }
        return
    }

    val uriHandler = LocalUriHandler.current
    val settingsManager = remember(settings) { SettingsManager(settings) }
    var currentCategory by remember { mutableStateOf(Category.RECRUITMENT) }
    var currentPage by remember { mutableIntStateOf(1) }
    var announcements by remember { mutableStateOf(emptyList<Announcement>()) }
    var isLoading by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val notificationManager: AppNotificationManager = rememberNotificationManager()

    var notificationsEnabled by remember { mutableStateOf(settingsManager.notificationsEnabled) }
    var syncInterval by remember { mutableIntStateOf(settingsManager.syncIntervalMinutes) }

    // 알림 설정이 변경될 때마다 배경 동기화 작업을 재설정합니다.
    LaunchedEffect(notificationsEnabled, syncInterval) {
        if (notificationsEnabled) {
            notificationManager.scheduleSync(syncInterval)
        } else {
            notificationManager.cancelSync()
        }
    }

    // 카테고리나 페이지가 변경될 때 데이터를 새로 고칩니다.
    LaunchedEffect(currentCategory, currentPage) {
        isLoading = true
        announcements = service.fetchAnnouncements(currentPage, currentCategory.key, currentCategory.bbsNo)
        
        // 첫 로드 시 마지막으로 본 공지 ID를 저장하여 이후 알림 판단 근거로 사용합니다.
        if (currentPage == 1 && announcements.isNotEmpty() && settingsManager.lastSeenId.isEmpty()) {
            settingsManager.lastSeenId = announcements.first().id
        }
        
        isLoading = false
    }

    AppContent(
        announcements = announcements,
        currentCategory = currentCategory,
        currentPage = currentPage,
        isLoading = isLoading,
        showSettings = showSettings,
        onCategorySelected = {
            currentCategory = it
            currentPage = 1
        },
        onPageSelected = { currentPage = it },
        onSettingsClick = { showSettings = true },
        onSettingsDismiss = { showSettings = false },
        onHomeClick = {
            currentCategory = Category.RECRUITMENT
            currentPage = 1
        },
        onAnnouncementClick = { announcement ->
            // 공지사항 클릭 시 시스템 브라우저를 통해 URL을 엽니다.
            if (announcement.url.isNotEmpty()) {
                uriHandler.openUri(announcement.url)
            }
        },
        notificationsEnabled = notificationsEnabled,
        syncInterval = syncInterval,
        onNotificationsChanged = { 
            settingsManager.notificationsEnabled = it
            notificationsEnabled = it
        },
        onSyncIntervalChanged = { 
            settingsManager.syncIntervalMinutes = it
            syncInterval = it
        },
        settingsManager = settingsManager
    )
}

/**
 * 앱의 주요 콘텐츠 화면을 구성하는 컴포저블입니다.
 * Scaffold를 사용하여 상단바, 하단바, 본문 영역을 관리합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    announcements: List<Announcement>,
    currentCategory: Category,
    currentPage: Int,
    isLoading: Boolean,
    showSettings: Boolean,
    onCategorySelected: (Category) -> Unit,
    onPageSelected: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsDismiss: () -> Unit,
    onHomeClick: () -> Unit,
    onAnnouncementClick: (Announcement) -> Unit,
    notificationsEnabled: Boolean,
    syncInterval: Int,
    onNotificationsChanged: (Boolean) -> Unit,
    onSyncIntervalChanged: (Int) -> Unit,
    settingsManager: SettingsManager
) {
    // Material 3 기반의 현대적인 보라색 계열 컬러 스킴 적용
    val colorScheme = lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color.White,
        surface = Color(0xFFFEF7FF),
        onSurface = Color(0xFF1D1B20),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F)
    )

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "보은 알리미",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        ) 
                    },
                    actions = {
                        IconButton(onClick = onHomeClick) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    PaginationBar(
                        currentPage = currentPage,
                        onPageSelected = onPageSelected
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // 카테고리 선택 탭
                PrimaryTabRow(
                    selectedTabIndex = currentCategory.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {}
                ) {
                    Category.entries.forEach { category ->
                        Tab(
                            selected = currentCategory == category,
                            onClick = { onCategorySelected(category) },
                            text = { 
                                Text(
                                    category.displayName,
                                    style = MaterialTheme.typography.titleSmall
                                ) 
                            }
                        )
                    }
                }

                if (isLoading) {
                    // 로딩 중일 때 중앙에 인디케이터 표시
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                } else {
                    // 공지사항 목록
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(announcements) { announcement ->
                            AnnouncementItem(
                                announcement = announcement,
                                onClick = { onAnnouncementClick(announcement) }
                            )
                        }
                    }
                }
            }
        }

        // 설정 다이얼로그
        if (showSettings) {
            SettingsDialog(
                notificationsEnabled = notificationsEnabled,
                syncInterval = syncInterval,
                onNotificationsChanged = onNotificationsChanged,
                onSyncIntervalChanged = onSyncIntervalChanged,
                onDismiss = onSettingsDismiss,
                settingsManager = settingsManager
            )
        }
    }
}

/**
 * 지원하는 공지사항 카테고리를 정의한 열거형 클래스입니다.
 */
enum class Category(val displayName: String, val key: Int, val bbsNo: Int) {
    RECRUITMENT("채용공고", 142, 68),
    NOTICES("고시공고", 194, 66)
}

/**
 * 개별 공지사항 항목을 카드 형태로 표시하는 컴포저블입니다.
 * 마감일이 지난 항목은 불투명하게 표시하여 시각적으로 구분합니다.
 */
@Composable
fun AnnouncementItem(announcement: Announcement, onClick: () -> Unit) {
    val today = remember { 
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date 
    }
    
    // 마감일이 지났는지 여부를 판단합니다.
    val isExpired = remember(announcement.deadlineDate) {
        announcement.deadlineDate?.let { dateStr ->
            try {
                val deadline = LocalDate.parse(dateStr.trim())
                deadline < today
            } catch (e: Exception) {
                false
            }
        } ?: false
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isExpired) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 부서 이름 표시 배지
                Surface(
                    color = if (isExpired) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = announcement.category ?: "일반",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isExpired) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                // 새로운 공지일 경우 NEW 배지 표시
                if (announcement.isNew && !isExpired) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFFF5252),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "NEW",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 공지 제목
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                ),
                color = if (isExpired) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 등록일 정보
                Column {
                    Text(
                        text = "작성일",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = announcement.publishDate,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isExpired) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 마감일 정보 (있는 경우에만 표시)
                announcement.deadlineDate?.let {
                    val deadlineColor = if (isExpired) MaterialTheme.colorScheme.outline else Color(0xFFFF5252)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "마감일",
                            style = MaterialTheme.typography.labelSmall,
                            color = deadlineColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = deadlineColor,
                            fontWeight = if (isExpired) FontWeight.Normal else FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 페이지 번호를 탐색하기 위한 하단 바 컴포저블입니다.
 * 5개 단위의 페이지 번호를 그룹화하여 표시합니다.
 */
@Composable
fun PaginationBar(currentPage: Int, onPageSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val startPage = ((currentPage - 1) / 5) * 5 + 1
        
        // 이전 페이지 그룹 버튼
        IconButton(
            onClick = { if (startPage > 1) onPageSelected(startPage - 1) },
            enabled = startPage > 1
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in startPage until startPage + 5) {
                val isSelected = i == currentPage
                Surface(
                    onClick = { onPageSelected(i) },
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = i.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 다음 페이지 그룹 버튼
        IconButton(
            onClick = { onPageSelected(startPage + 5) }
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}

/**
 * 앱의 설정을 변경할 수 있는 다이얼로그 컴포저블입니다.
 * 알림 여부, 동기화 주기를 설정할 수 있으며 관리자 모드를 제공합니다.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsDialog(
    notificationsEnabled: Boolean,
    syncInterval: Int,
    onNotificationsChanged: (Boolean) -> Unit,
    onSyncIntervalChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    settingsManager: SettingsManager
) {
    var currentNotificationsEnabled by remember { mutableStateOf(notificationsEnabled) }
    var currentSyncInterval by remember { mutableIntStateOf(syncInterval) }
    var showAdminLogs by remember { mutableStateOf(false) }
    var versionClickCount by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("설정") },
        text = {
            Column {
                /**
                 * 관리자 모드(로직 설명):
                 * 하단의 버전 텍스트를 7번 연속으로 클릭하면 관리자 로그가 표시됩니다.
                 * 배경 동기화 성공/실패 여부를 확인할 수 있습니다.
                 */
                if (showAdminLogs) {
                    Text(
                        "관리자 동기화 로그",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(settingsManager.getSyncLogs()) { log ->
                                Text(log, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    TextButton(
                        onClick = { showAdminLogs = false },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("로그 닫기") }
                    Spacer(Modifier.height(16.dp))
                }

                // 알림 토글
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("알림 사용")
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = currentNotificationsEnabled,
                        onCheckedChange = {
                            currentNotificationsEnabled = it
                            onNotificationsChanged(it)
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
                
                // 동기화 주기 텍스트 표시
                val days = currentSyncInterval / 1440
                val hours = (currentSyncInterval % 1440) / 60
                val mins = currentSyncInterval % 60
                val timeText = buildString {
                    if (days > 0) append("${days}일 ")
                    if (hours > 0) append("${hours}시간 ")
                    if (mins > 0 || isEmpty()) append("${mins}분")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("동기화 주기: $timeText")
                }
                
                var hoursPart by remember { mutableStateOf((currentSyncInterval / 60).toString()) }
                var minsPart by remember { mutableStateOf((currentSyncInterval % 60).toString()) }
                
                // 동기화 주기 입력을 위한 텍스트 필드
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = hoursPart,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                hoursPart = newValue
                                val h = newValue.toIntOrNull() ?: 0
                                val m = minsPart.toIntOrNull() ?: 0
                                val total = (h * 60 + m).coerceIn(15, 7200) // 최소 15분, 최대 5일
                                currentSyncInterval = total
                                onSyncIntervalChanged(total)
                            }
                        },
                        label = { Text("시간", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    TextField(
                        value = minsPart,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                minsPart = newValue
                                val h = hoursPart.toIntOrNull() ?: 0
                                val m = newValue.toIntOrNull() ?: 0
                                val total = (h * 60 + m).coerceIn(15, 7200)
                                currentSyncInterval = total
                                onSyncIntervalChanged(total)
                            }
                        },
                        label = { Text("분", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                // 동기화 주기 조절 슬라이더
                Slider(
                    value = currentSyncInterval.toFloat(),
                    onValueChange = { 
                        currentSyncInterval = it.toInt()
                        hoursPart = (it.toInt() / 60).toString()
                        minsPart = (it.toInt() % 60).toString()
                        onSyncIntervalChanged(it.toInt())
                    },
                    valueRange = 15f..7200f,
                    steps = 478
                )

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                
                // 버전 텍스트 (7회 클릭 시 관리자 모드 진입)
                Text(
                    text = "버전 ${getAppVersion()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            versionClickCount++
                            if (versionClickCount >= 7) {
                                showAdminLogs = true
                                versionClickCount = 0
                            }
                        }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        }
    )
}
