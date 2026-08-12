package com.boeun.announcement.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import androidx.compose.ui.tooling.preview.Preview

/**
 * 앱 실행 시 표시되는 스플래시 화면입니다.
 * 그라데이션 배경과 보은군의 정체성을 담은 로고 애니메이션을 포함합니다.
 */
@Composable
fun BoeunSplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2C2F48),
                        Color(0xFF0D0F1A)
                    ),
                    center = Offset.Unspecified,
                    radius = 2500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        LogoContainer()
    }
}

/**
 * 스플래시 화면의 중앙 로고 컨테이너입니다.
 * 원형의 종이 질감 배경과 텍스트를 포함합니다.
 */
@Composable
fun LogoContainer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(360.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(80.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .border(
                width = 12.dp,
                color = Color(0xFF1E2036),
                shape = RoundedCornerShape(80.dp)
            )
            .clip(RoundedCornerShape(80.dp))
            .background(Color(0xFFF7F4EB))
    ) {
        // 전통적인 종이 질감을 표현하는 캔버스 레이어
        PaperTexture()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 한자 '보은' 스타일의 텍스트
            Text(
                text = "보은",
                fontSize = 140.sp,
                lineHeight = 140.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-15).dp)
            )

            // 한글 '보은' 텍스트
            Text(
                text = "보은",
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF3A3A3A),
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 캔버스를 사용하여 종이 질감(점, 선)을 무작위로 그리는 함수입니다.
 */
@Composable
private fun PaperTexture() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val random = Random(42)
        
        // 금색 점들을 그려서 한지 느낌을 줌
        repeat(80) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val fleckSize = random.nextFloat() * 4f + 1f
            val alpha = random.nextFloat() * 0.4f + 0.1f
            drawCircle(
                color = Color(0xFFD4AF37).copy(alpha = alpha),
                radius = fleckSize,
                center = Offset(x, y)
            )
        }
        
        // 미세한 선들을 그려서 종이 결 느낌을 줌
        repeat(50) {
            val x1 = random.nextFloat() * size.width
            val y1 = random.nextFloat() * size.height
            val length = random.nextFloat() * 40f + 15f
            val angle = random.nextFloat() * 360f
            val x2 = x1 + length * kotlin.math.cos(angle.toDouble()).toFloat()
            val y2 = y1 + length * kotlin.math.sin(angle.toDouble()).toFloat()
            
            drawLine(
                color = Color(0xFF8B4513).copy(alpha = 0.06f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 0.6f
            )
        }
    }
}

@Preview
@Composable
fun BoeunSplashScreenPreview() {
    MaterialTheme {
        BoeunSplashScreen()
    }
}
