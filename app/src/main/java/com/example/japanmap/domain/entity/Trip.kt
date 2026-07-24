package com.example.japanmap.domain.entity

import java.util.UUID

/**
 * 특정 도도부현을 방문한 1회의 여행 기록. iOS `Trip.swift` 포팅.
 * 날짜는 epoch millis(Long)로 표현.
 */
data class Trip(
    val id: UUID,
    val prefectureID: PrefectureID,
    /** 선택한 하위 여행지 id (예: "hokkaido-sapporo"). 미선택 시 null. */
    val subRegionID: String?,
    /** 여행 시작일 (epoch millis). */
    val visitedAt: Long,
    /** 여행 종료일 (epoch millis). 단일 날짜 여행은 null 또는 visitedAt과 동일. */
    val endDate: Long?,
    val memo: String,
    val photos: List<TripPhoto>,
)
