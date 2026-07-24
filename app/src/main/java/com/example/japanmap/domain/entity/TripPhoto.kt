package com.example.japanmap.domain.entity

import java.util.UUID

/**
 * 여행 기록에 첨부된 사진 1장. iOS `TripPhoto.swift` 포팅.
 * 실제 이미지 바이너리는 앱 파일 디렉토리에 저장하고, 본 엔티티는 식별자와 파일명만 보관.
 */
data class TripPhoto(
    val id: UUID,
    val fileName: String,
    val createdAt: Long,
)
