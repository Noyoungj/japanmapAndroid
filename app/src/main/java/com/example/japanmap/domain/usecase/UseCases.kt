package com.example.japanmap.domain.usecase

import com.example.japanmap.domain.entity.PrefectureID
import com.example.japanmap.domain.entity.Trip
import com.example.japanmap.domain.entity.TripPhoto
import com.example.japanmap.domain.repository.PhotoStorageRepository
import com.example.japanmap.domain.repository.TripRepository
import java.util.UUID

/** 새 여행 기록을 영속화한다. iOS `AddTripUseCase` 포팅. */
class AddTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(trip: Trip): Trip = repository.add(trip)
}

/** 여행 기록을 영구 삭제한다. iOS `DeleteTripUseCase` 포팅. */
class DeleteTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(tripID: UUID) = repository.delete(tripID)
}

/** 특정 도도부현의 여행 기록을 방문일 내림차순으로 조회. iOS `FetchTripsUseCase` 포팅. */
class FetchTripsUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(prefectureID: PrefectureID): List<Trip> =
        repository.fetch(prefectureID)
}

/** 방문 진행률. iOS `CalculateVisitProgressUseCase` 포팅. */
data class VisitProgress(
    val visited: Set<PrefectureID>,
    val total: Int,
) {
    val visitedCount: Int get() = visited.size
    val ratio: Double get() = if (total > 0) visited.size.toDouble() / total else 0.0
}

class CalculateVisitProgressUseCase(
    private val repository: TripRepository,
    private val totalPrefectures: Int = 47,
) {
    suspend operator fun invoke(): VisitProgress =
        VisitProgress(repository.fetchVisitedPrefectureIDs(), totalPrefectures)
}

/**
 * 이미지 바이너리를 파일 저장 후 `TripPhoto` 메타데이터를 반환. iOS `SavePhotoUseCase` 포팅.
 */
class SavePhotoUseCase(private val storage: PhotoStorageRepository) {
    suspend operator fun invoke(imageData: ByteArray): TripPhoto {
        val fileName = storage.save(imageData)
        return TripPhoto(id = UUID.randomUUID(), fileName = fileName, createdAt = System.currentTimeMillis())
    }
}
