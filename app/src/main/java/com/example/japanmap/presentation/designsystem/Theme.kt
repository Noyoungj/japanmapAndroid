package com.example.japanmap.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DesignColors.Brand.primary,
    secondary = DesignColors.Brand.secondary,
    background = DesignColors.Paper.canvas,
    surface = DesignColors.Paper.canvas,
)

@Composable
fun JapanMapTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // iOS와 동일하게 라이트 페이퍼 톤 고정 (다크모드 대응은 후속 과제).
    MaterialTheme(colorScheme = LightColors, content = content)
}
