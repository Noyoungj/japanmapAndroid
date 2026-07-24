package com.example.japanmap.domain.entity

/**
 * 도도부현 하위 지역 (사용자 정의 여행 권역). iOS `SubRegion.swift` 포팅.
 * 시정촌 같은 행정 단위가 아니라 "삿포로", "후라노"처럼 여행자가 인지하는 지명.
 */
data class SubRegion(
    /** 전역 고유 ID (예: "hokkaido-sapporo"). */
    val id: String,
    /** 한국어 표시명. */
    val ko: String,
    /** 라벨/핀 위치 — prefectures.json과 동일한 1000×1000 좌표계. */
    val centroid: Centroid?,
    /** 실제 지리 좌표 (위도/경도). */
    val coordinate: Coordinate?,
    /** SVG path (M/L/Z). 추후 영역 표시용. */
    val path: String?,
) {
    val displayName: String get() = ko

    data class Centroid(val cx: Double, val cy: Double)
    data class Coordinate(val lat: Double, val lon: Double)
}
