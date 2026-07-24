package com.example.japanmap.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.Region
import com.example.japanmap.domain.entity.SubRegion
import com.example.japanmap.presentation.designsystem.DesignColors
import com.example.japanmap.presentation.designsystem.PrefectureColorMapper
import com.example.japanmap.presentation.designsystem.PrefectureColorMapper.MapMode
import com.example.japanmap.presentation.designsystem.PrefectureColorMapper.PrefectureState

/**
 * 상세 화면 상단 지도 대체 — 해당 도도부현 shape + 방문한 여행지 핀.
 * (iOS는 MapKit 실지도. 여기선 SVG shape + centroid 핀으로 경량 재현.)
 */
@Composable
fun PrefectureShapeCard(
    prefecture: Prefecture,
    region: Region,
    visitedSubRegions: List<SubRegion>,
    modifier: Modifier = Modifier,
) {
    val parsed = remember(prefecture.num) { SvgPathParser.parse(prefecture.path) }
    val fill = PrefectureColorMapper.fillColor(region, MapMode.ACTIVE, PrefectureState.DEFAULT)

    Canvas(modifier.fillMaxSize()) {
        val points = parsed.subPaths.flatten()
        if (points.isEmpty()) return@Canvas
        val minX = points.minOf { it.x }; val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }; val maxY = points.maxOf { it.y }
        val bw = (maxX - minX).coerceAtLeast(1f); val bh = (maxY - minY).coerceAtLeast(1f)
        val pad = 24f
        val s = minOf((size.width - pad * 2) / bw, (size.height - pad * 2) / bh)
        val drawnW = bw * s; val drawnH = bh * s
        val tx = (size.width - drawnW) / 2f; val ty = (size.height - drawnH) / 2f
        val map: (Offset) -> Offset = { p -> Offset((p.x - minX) * s + tx, (p.y - minY) * s + ty) }

        val path = parsed.toComposePath(map)
        drawPath(path, color = fill)
        drawPath(path, color = DesignColors.Stroke.activeDefault, style = Stroke(width = 1.5f))

        // 방문 여행지 핀 (centroid 기준)
        visitedSubRegions.forEach { sr ->
            val c = sr.centroid ?: return@forEach
            val p = map(Offset(c.cx.toFloat(), c.cy.toFloat()))
            drawCircle(Color.White, radius = 7f, center = p)
            drawCircle(DesignColors.Brand.secondary, radius = 5f, center = p)
        }
    }
}
