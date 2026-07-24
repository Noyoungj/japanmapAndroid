package com.example.japanmap.presentation.designsystem

import androidx.compose.ui.graphics.Color
import com.example.japanmap.domain.entity.Region

/**
 * (Region, MapMode, PrefectureState) 조합을 Color로 매핑. iOS `PrefectureColorMapper` 포팅.
 */
object PrefectureColorMapper {

    enum class MapMode { ACTIVE, INACTIVE }
    enum class PrefectureState { DEFAULT, HIGHLIGHTED, SELECTED }

    /** 다이어리 실험: active fill에 흰색 혼합 비율. */
    private const val ACTIVE_PASTEL_MIX = 0.15f

    fun fillColor(region: Region, mode: MapMode, state: PrefectureState): Color = when (mode) {
        MapMode.INACTIVE -> inactiveColor(region)
        MapMode.ACTIVE -> pastelize(activeColor(region, state), ACTIVE_PASTEL_MIX)
    }

    private fun pastelize(color: Color, ratio: Float): Color {
        val t = ratio.coerceIn(0f, 1f)
        return Color(
            red = color.red * (1 - t) + t,
            green = color.green * (1 - t) + t,
            blue = color.blue * (1 - t) + t,
            alpha = color.alpha,
        )
    }

    /** 다이어리 실험: selected 검정 stroke 제거 — mode만 반영. */
    fun strokeColor(mode: MapMode, @Suppress("UNUSED_PARAMETER") state: PrefectureState): Color =
        if (mode == MapMode.INACTIVE) DesignColors.Stroke.inactive else DesignColors.Stroke.activeDefault

    fun strokeWidth(mode: MapMode, state: PrefectureState): Float = when (mode) {
        MapMode.INACTIVE -> 1.0f
        MapMode.ACTIVE -> when (state) {
            PrefectureState.DEFAULT, PrefectureState.SELECTED -> 1.5f
            PrefectureState.HIGHLIGHTED -> 2.0f
        }
    }

    fun labelColor(mode: MapMode): Color =
        if (mode == MapMode.ACTIVE) DesignColors.Label.active else DesignColors.Label.inactive

    fun canvasColor(mode: MapMode): Color =
        if (mode == MapMode.ACTIVE) DesignColors.Background.canvasActive else DesignColors.Background.canvasInactive

    private fun activeColor(region: Region, state: PrefectureState): Color {
        // 다이어리 실험: selected에서도 default 톤 유지.
        val s = if (state == PrefectureState.SELECTED) PrefectureState.DEFAULT else state
        return when (region) {
            Region.HOKKAIDO -> pick(s, DesignColors.Region.Hokkaido.default, DesignColors.Region.Hokkaido.highlighted, DesignColors.Region.Hokkaido.selected)
            Region.TOHOKU -> pick(s, DesignColors.Region.Tohoku.default, DesignColors.Region.Tohoku.highlighted, DesignColors.Region.Tohoku.selected)
            Region.KANTO -> pick(s, DesignColors.Region.Kanto.default, DesignColors.Region.Kanto.highlighted, DesignColors.Region.Kanto.selected)
            Region.CHUBU -> pick(s, DesignColors.Region.Chubu.default, DesignColors.Region.Chubu.highlighted, DesignColors.Region.Chubu.selected)
            Region.KANSAI -> pick(s, DesignColors.Region.Kansai.default, DesignColors.Region.Kansai.highlighted, DesignColors.Region.Kansai.selected)
            Region.CHUGOKU -> pick(s, DesignColors.Region.Chugoku.default, DesignColors.Region.Chugoku.highlighted, DesignColors.Region.Chugoku.selected)
            Region.SHIKOKU -> pick(s, DesignColors.Region.Shikoku.default, DesignColors.Region.Shikoku.highlighted, DesignColors.Region.Shikoku.selected)
            Region.KYUSHU -> pick(s, DesignColors.Region.Kyushu.default, DesignColors.Region.Kyushu.highlighted, DesignColors.Region.Kyushu.selected)
        }
    }

    private fun pick(s: PrefectureState, d: Color, h: Color, sel: Color): Color = when (s) {
        PrefectureState.DEFAULT -> d
        PrefectureState.HIGHLIGHTED -> h
        PrefectureState.SELECTED -> sel
    }

    private fun inactiveColor(region: Region): Color = when (region) {
        Region.HOKKAIDO -> DesignColors.Inactive.hokkaido
        Region.TOHOKU -> DesignColors.Inactive.tohoku
        Region.KANTO -> DesignColors.Inactive.kanto
        Region.CHUBU -> DesignColors.Inactive.chubu
        Region.KANSAI -> DesignColors.Inactive.kansai
        Region.CHUGOKU -> DesignColors.Inactive.chugoku
        Region.SHIKOKU -> DesignColors.Inactive.shikoku
        Region.KYUSHU -> DesignColors.Inactive.kyushu
    }
}
