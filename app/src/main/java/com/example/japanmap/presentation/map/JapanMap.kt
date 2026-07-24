package com.example.japanmap.presentation.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.PrefectureID
import com.example.japanmap.presentation.designsystem.DesignColors
import com.example.japanmap.presentation.designsystem.PrefectureColorMapper
import com.example.japanmap.presentation.designsystem.PrefectureColorMapper.MapMode
import com.example.japanmap.presentation.designsystem.PrefectureColorMapper.PrefectureState

private const val VIEW_BOX = 1000f
private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 3f
private val NARROW_NUMBERS = setOf(11, 12, 13, 14, 21, 25, 26, 27, 28, 29, 30, 36, 37, 41, 44)

private const val OKINAWA_ID = 47
private const val OKINAWA_SCALE = 2.0f
private val OKINAWA_OFFSET = Offset(-30f, 20f)
// 인셋 사각 테두리 (SVG 좌표): x=744, y=783.5, w=212, h=234
private val OKINAWA_RECT = floatArrayOf(744f, 783.5f, 744f + 212f, 783.5f + 234f)

/** prefecture별 파싱 결과 캐시 (오키나와 변형 반영). */
private data class MapModel(
    val prefecture: Prefecture,
    val parsed: SvgPathParser.ParsedPath,
)

private fun buildModels(prefectures: List<Prefecture>): List<MapModel> = prefectures.map { p ->
    var parsed = SvgPathParser.parse(p.path)
    if (p.num == OKINAWA_ID) {
        val c = Offset(p.centroid.cx.toFloat(), p.centroid.cy.toFloat())
        parsed = SvgPathParser.ParsedPath(
            parsed.subPaths.map { poly ->
                poly.map { pt ->
                    Offset(
                        (pt.x - c.x) * OKINAWA_SCALE + c.x + OKINAWA_OFFSET.x,
                        (pt.y - c.y) * OKINAWA_SCALE + c.y + OKINAWA_OFFSET.y,
                    )
                }
            },
        )
    }
    MapModel(p, parsed)
}

/**
 * 47개 도도부현 인터랙티브 SVG 맵. iOS `JapanMapView` 포팅.
 *
 * @param visitedPrefectureIDs 방문 = 컬러, 미방문 = 회색.
 * @param selectedPrefectureID 현재 선택된 도도부현 (테두리 강조/라벨).
 * @param mode active/inactive.
 * @param onPrefectureTapped 탭된 도도부현 통보 (토글은 호출 측에서 처리).
 */
@Composable
fun JapanMap(
    prefectures: List<Prefecture>,
    visitedPrefectureIDs: Set<PrefectureID>,
    selectedPrefectureID: PrefectureID?,
    mode: MapMode,
    onPrefectureTapped: (PrefectureID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val models = remember(prefectures) { buildModels(prefectures) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val canvasColor = PrefectureColorMapper.canvasColor(mode)

    Box(
        modifier = modifier
            .background(canvasColor)
            .pointerInput(models, mode) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    offset = centroid - ((centroid - offset) / scale) * newScale + pan
                    scale = newScale
                    if (newScale <= MIN_ZOOM) offset = Offset.Zero
                }
            }
            .pointerInput(models, mode, scale, offset) {
                detectTapGestures(
                    onDoubleTap = { tap ->
                        if (scale > MIN_ZOOM) {
                            scale = 1f; offset = Offset.Zero
                        } else {
                            val newScale = 2f
                            offset = tap - ((tap - offset) / scale) * newScale
                            scale = newScale
                        }
                    },
                    onTap = { tap ->
                        if (mode != MapMode.ACTIVE) return@detectTapGestures
                        val fit = fitTransform(size.width.toFloat(), size.height.toFloat())
                        // 화면 → fit → svg 역변환
                        val fitPoint = (tap - offset) / scale
                        val svg = Offset(
                            (fitPoint.x - fit.tx) / fit.scale,
                            (fitPoint.y - fit.ty) / fit.scale,
                        )
                        hitTest(models, svg)?.let(onPrefectureTapped)
                    },
                )
            },
    ) {
        val animatedScale by animateFloatAsState(scale, label = "zoom")
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fit = fitTransform(size.width, size.height)
            val svgToView: (Offset) -> Offset = { p ->
                Offset(
                    (p.x * fit.scale + fit.tx) * scale + offset.x,
                    (p.y * fit.scale + fit.ty) * scale + offset.y,
                )
            }

            // 1) shape
            for (m in models) {
                val state = if (m.prefecture.num == selectedPrefectureID) PrefectureState.SELECTED else PrefectureState.DEFAULT
                val effMode = effectiveMode(mode, m.prefecture.num, visitedPrefectureIDs)
                val path = m.parsed.toComposePath(svgToView)
                drawPath(path, color = PrefectureColorMapper.fillColor(m.prefecture.region, effMode, state))
                drawPath(
                    path,
                    color = PrefectureColorMapper.strokeColor(effMode, state),
                    style = Stroke(width = PrefectureColorMapper.strokeWidth(effMode, state) * scale),
                )
            }

            // 2) 오키나와 인셋 테두리 (dashed)
            drawOkinawaFrame(svgToView)

            // 3) 라벨 (줌 의존 opacity + counter-scale)
            val labelAlpha = (((animatedScale - 1.2f) / 0.5f)).coerceIn(0f, 1f)
            if (labelAlpha > 0.01f) {
                drawLabels(models, svgToView, animatedScale, labelAlpha, mode, density.density)
            }
        }
    }
}

private data class Fit(val scale: Float, val tx: Float, val ty: Float)

private fun fitTransform(w: Float, h: Float): Fit {
    val s = minOf(w, h) / VIEW_BOX
    val scaled = VIEW_BOX * s
    return Fit(s, (w - scaled) / 2f, (h - scaled) / 2f)
}

private fun effectiveMode(mode: MapMode, id: PrefectureID, visited: Set<PrefectureID>): MapMode {
    if (mode == MapMode.INACTIVE) return MapMode.INACTIVE
    return if (visited.contains(id)) MapMode.ACTIVE else MapMode.INACTIVE
}

/** path.contains 우선, 실패 시 반경 25 SVG units 내 최근접 centroid. iOS와 동일. */
private fun hitTest(models: List<MapModel>, svg: Offset): PrefectureID? {
    for (m in models) {
        if (m.parsed.contains(svg)) return m.prefecture.num
    }
    val maxDistSq = 25f * 25f
    var nearest: PrefectureID? = null
    var nearestSq = Float.MAX_VALUE
    for (m in models) {
        val dx = svg.x - m.prefecture.centroid.cx.toFloat()
        val dy = svg.y - m.prefecture.centroid.cy.toFloat()
        val d = dx * dx + dy * dy
        if (d < nearestSq && d <= maxDistSq) { nearestSq = d; nearest = m.prefecture.num }
    }
    return nearest
}

private fun DrawScope.drawOkinawaFrame(svgToView: (Offset) -> Offset) {
    val tl = svgToView(Offset(OKINAWA_RECT[0], OKINAWA_RECT[1]))
    val br = svgToView(Offset(OKINAWA_RECT[2], OKINAWA_RECT[3]))
    val path = androidx.compose.ui.graphics.Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = tl.x, top = tl.y, right = br.x, bottom = br.y,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f * (br.x - tl.x) / 212f),
            ),
        )
    }
    drawPath(
        path,
        color = DesignColors.Brand.primary.copy(alpha = 0.3f),
        style = Stroke(
            width = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
        ),
    )
}

private fun DrawScope.drawLabels(
    models: List<MapModel>,
    svgToView: (Offset) -> Offset,
    scale: Float,
    alpha: Float,
    mode: MapMode,
    density: Float,
) {
    val labelColor = PrefectureColorMapper.labelColor(mode).toArgb()
    val outlineColor = DesignColors.Label.outline.toArgb()
    drawContext.canvas.nativeCanvas.apply {
        for (m in models) {
            val p = m.prefecture
            val basePt = 0f + if (NARROW_NUMBERS.contains(p.num)) 9f else 11f
            // counter-scale: 캔버스가 scale배 확대되므로 글자를 1/scale로 그려 화면 크기 일정.
            val textSizePx = basePt * density
            val pos = svgToView(Offset(p.centroid.cx.toFloat(), p.centroid.cy.toFloat()))

            // 흰색 외곽선
            val stroke = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = textSizePx
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f
                color = outlineColor
                this.alpha = (alpha * 255).toInt()
            }
            val fill = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = textSizePx
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                color = labelColor
                this.alpha = (alpha * 255).toInt()
            }
            val baselineOffset = (fill.descent() + fill.ascent()) / 2f
            drawText(p.ko, pos.x, pos.y - baselineOffset, stroke)
            drawText(p.ko, pos.x, pos.y - baselineOffset, fill)
        }
    }
}
