package com.example.japanmap.presentation.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/**
 * SVG path 문자열(M/L/Z 절대좌표)을 파싱. iOS `SVGPathParser` 포팅.
 *
 * 렌더링용 Compose [Path]와, hit-test용 폴리곤 목록(sub-path별 정점 리스트)을
 * 함께 만들어 준다. 한 prefecture에 여러 sub-path(본섬+부속섬)가 있으면
 * Z 이후 새 M이 나올 때 새 sub-path로 이어 붙인다.
 */
object SvgPathParser {

    /** 파싱 결과 — SVG viewBox(0..1000) 좌표계 기준. */
    data class ParsedPath(
        val subPaths: List<List<Offset>>,
    ) {
        /** Compose 렌더링용 Path 생성 (transform은 Canvas에서 적용). */
        fun toComposePath(transform: (Offset) -> Offset): Path {
            val path = Path()
            for (poly in subPaths) {
                if (poly.isEmpty()) continue
                val first = transform(poly.first())
                path.moveTo(first.x, first.y)
                for (i in 1 until poly.size) {
                    val p = transform(poly[i])
                    path.lineTo(p.x, p.y)
                }
                path.close()
            }
            return path
        }

        /** SVG 좌표계 점이 이 도형(어느 sub-path든) 내부인지 ray-casting 판정. */
        fun contains(point: Offset): Boolean = subPaths.any { pointInPolygon(point, it) }
    }

    fun parse(pathString: String): ParsedPath {
        val tokens = tokenize(pathString)
        val subPaths = mutableListOf<List<Offset>>()
        var current = mutableListOf<Offset>()
        var i = 0
        while (i < tokens.size) {
            when (tokens[i]) {
                "M" -> {
                    if (current.isNotEmpty()) {
                        subPaths.add(current)
                        current = mutableListOf()
                    }
                    val x = tokens[i + 1].toFloat()
                    val y = tokens[i + 2].toFloat()
                    current.add(Offset(x, y))
                    i += 3
                }
                "L" -> {
                    val x = tokens[i + 1].toFloat()
                    val y = tokens[i + 2].toFloat()
                    current.add(Offset(x, y))
                    i += 3
                }
                "Z" -> i += 1
                else -> i += 1
            }
        }
        if (current.isNotEmpty()) subPaths.add(current)
        return ParsedPath(subPaths)
    }

    private fun tokenize(s: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        fun flush() {
            if (current.isNotEmpty()) { tokens.add(current.toString()); current.clear() }
        }
        for (c in s) {
            when (c) {
                'M', 'L', 'Z', 'm', 'l', 'z' -> { flush(); tokens.add(c.uppercaseChar().toString()) }
                ' ', ',', '\t', '\n' -> flush()
                else -> current.append(c)
            }
        }
        flush()
        return tokens
    }

    /** 표준 ray-casting point-in-polygon. */
    private fun pointInPolygon(p: Offset, poly: List<Offset>): Boolean {
        if (poly.size < 3) return false
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val xi = poly[i].x; val yi = poly[i].y
            val xj = poly[j].x; val yj = poly[j].y
            val intersect = (yi > p.y) != (yj > p.y) &&
                p.x < (xj - xi) * (p.y - yi) / (yj - yi) + xi
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }
}
