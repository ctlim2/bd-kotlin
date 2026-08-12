package com.boeun.announcement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    AppContent(
        announcements = listOf(
            Announcement(
                id = "1",
                title = "보은군 채용 공고 테스트 1",
                publishDate = "2024-03-20",
                deadlineDate = "2024-03-31",
                url = "https://www.boeun.go.kr",
                category = "총무과",
                bbsNo = 68
            )
        ),
        currentCategory = Category.RECRUITMENT,
        currentPage = 1,
        isLoading = false,
        showSettings = false,
        onCategorySelected = {},
        onPageSelected = {},
        onSettingsClick = {},
        onSettingsDismiss = {},
        onHomeClick = {},
        onAnnouncementClick = {},
        notificationsEnabled = true,
        syncInterval = 60,
        onNotificationsChanged = {},
        onSyncIntervalChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsDialogPreview() {
    SettingsDialog(
        notificationsEnabled = true,
        syncInterval = 1500,
        onNotificationsChanged = {},
        onSyncIntervalChanged = {},
        onDismiss = {}
    )
}
