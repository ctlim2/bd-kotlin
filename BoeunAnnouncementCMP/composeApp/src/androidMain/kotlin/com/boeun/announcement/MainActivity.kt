package com.boeun.announcement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * 안드로이드 애플리케이션의 메인 엔트리 포인트입니다.
 * 엣지-투-엣지(Edge-to-edge) 설정을 적용하고 메인 UI(App)를 로드합니다.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Android 13(API 33) 이상에서는 알림 권한을 명시적으로 요청해야 합니다.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }
        
        enableEdgeToEdge()
        setContent {
            // 공통 모듈에 정의된 App 컴포저블을 호출합니다.
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
        onSyncIntervalChanged = {},
        settingsManager = SettingsManager(FakeSettings())
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsDialogPreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6750A4),
            surface = Color(0xFFFEF7FF),
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SettingsDialog(
                notificationsEnabled = true,
                syncInterval = 60,
                onNotificationsChanged = {},
                onSyncIntervalChanged = {},
                onDismiss = {},
                settingsManager = SettingsManager(FakeSettings())
            )
        }
    }
}

class FakeSettings : com.russhwolf.settings.Settings {
    private val map = mutableMapOf<String, Any>()
    override val keys: Set<String> get() = map.keys
    override val size: Int get() = map.size
    override fun clear() = map.clear()
    override fun remove(key: String) { map.remove(key) }
    override fun hasKey(key: String): Boolean = map.containsKey(key)
    
    override fun putInt(key: String, value: Int) { map[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = map[key] as? Int ?: defaultValue
    override fun getIntOrNull(key: String): Int? = map[key] as? Int
    
    override fun putLong(key: String, value: Long) { map[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = map[key] as? Long ?: defaultValue
    override fun getLongOrNull(key: String): Long? = map[key] as? Long
    
    override fun putString(key: String, value: String) { map[key] = value }
    override fun getString(key: String, defaultValue: String): String = map[key] as? String ?: defaultValue
    override fun getStringOrNull(key: String): String? = map[key] as? String
    
    override fun putFloat(key: String, value: Float) { map[key] = value }
    override fun getFloat(key: String, defaultValue: Float): Float = map[key] as? Float ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = map[key] as? Float
    
    override fun putDouble(key: String, value: Double) { map[key] = value }
    override fun getDouble(key: String, defaultValue: Double): Double = map[key] as? Double ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = map[key] as? Double
    
    override fun putBoolean(key: String, value: Boolean) { map[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = map[key] as? Boolean ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = map[key] as? Boolean
}
