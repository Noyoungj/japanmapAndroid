package com.example.japanmap.domain.entity

/** 도도부현 식별자. 1 ~ 47. */
typealias PrefectureID = Int

/**
 * 일본의 47개 도도부현 단위 엔티티 (불변값). iOS `Prefecture.swift` 포팅.
 * `assets/prefectures.json`에서 로드된 정적 데이터를 표현한다.
 */
data class Prefecture(
    val num: PrefectureID,
    /** 한국어 이름 (예: "홋카이도"). */
    val ko: String,
    /** 영문 키 (예: "hokkaido"). */
    val en: String,
    val region: Region,
    /** SVG 라벨 중심 좌표. */
    val centroid: Centroid,
    /** SVG path 문자열 (M / L / Z 명령, 절대 좌표). */
    val path: String,
) {
    val id: PrefectureID get() = num

    data class Centroid(val cx: Double, val cy: Double)
}
