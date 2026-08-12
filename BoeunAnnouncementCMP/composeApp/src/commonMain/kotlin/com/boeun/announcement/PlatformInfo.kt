package com.boeun.announcement

import androidx.compose.runtime.Composable

/**
 * 플랫폼별 앱 버전 정보를 가져오기 위한 expect 함수입니다.
 * Android와 iOS 각각의 패키지 정보에서 버전을 추출하도록 구현됩니다.
 */
@Composable
expect fun getAppVersion(): String
