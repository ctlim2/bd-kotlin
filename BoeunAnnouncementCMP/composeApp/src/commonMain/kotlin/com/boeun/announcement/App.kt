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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun App(
    service: AnnouncementService = remember { AnnouncementService() },
    settings: com.russhwolf.settings.Settings = remember { com.russhwolf.settings.Settings() }
) {
    val uriHandler = LocalUriHandler.current
    val settingsManager = remember(settings) { SettingsManager(settings) }
    var currentCategory by remember { mutableStateOf(Category.RECRUITMENT) }
    var currentPage by remember { mutableIntStateOf(1) }
    var announcements by remember { mutableStateOf(emptyList<Announcement>()) }
    var isLoading by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(currentCategory, currentPage) {
        isLoading = true
        announcements = service.fetchAnnouncements(currentPage, currentCategory.key, currentCategory.bbsNo)
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
            if (announcement.url.isNotEmpty()) {
                uriHandler.openUri(announcement.url)
            }
        },
        notificationsEnabled = settingsManager.notificationsEnabled,
        syncInterval = settingsManager.syncIntervalMinutes,
        onNotificationsChanged = { settingsManager.notificationsEnabled = it },
        onSyncIntervalChanged = { settingsManager.syncIntervalMinutes = it }
    )
}

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
    onSyncIntervalChanged: (Int) -> Unit
) {
    // 현대적인 컬러 스킴 정의 (Material 3 테마 활용)
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                } else {
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

        if (showSettings) {
            SettingsDialog(
                notificationsEnabled = notificationsEnabled,
                syncInterval = syncInterval,
                onNotificationsChanged = onNotificationsChanged,
                onSyncIntervalChanged = onSyncIntervalChanged,
                onDismiss = onSettingsDismiss
            )
        }
    }
}

enum class Category(val displayName: String, val key: Int, val bbsNo: Int) {
    RECRUITMENT("채용공고", 142, 68),
    NOTICES("고시공고", 194, 66)
}

@Composable
fun AnnouncementItem(announcement: Announcement, onClick: () -> Unit) {
    val today = remember { 
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date 
    }
    
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

        IconButton(
            onClick = { onPageSelected(startPage + 5) }
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}

@Composable
fun SettingsDialog(
    notificationsEnabled: Boolean,
    syncInterval: Int,
    onNotificationsChanged: (Boolean) -> Unit,
    onSyncIntervalChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentNotificationsEnabled by remember { mutableStateOf(notificationsEnabled) }
    var currentSyncInterval by remember { mutableIntStateOf(syncInterval) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("설정") },
        text = {
            Column {
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
                                val total = (h * 60 + m).coerceIn(15, 7200)
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
                Text(
                    text = "버전 $appVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        }
    )
}
