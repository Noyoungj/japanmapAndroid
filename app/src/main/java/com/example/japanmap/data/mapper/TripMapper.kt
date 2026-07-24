package com.example.japanmap.data.mapper

import com.example.japanmap.data.local.db.PhotoDbEntity
import com.example.japanmap.data.local.db.TripDbEntity
import com.example.japanmap.data.local.db.TripWithPhotos
import com.example.japanmap.domain.entity.Trip
import com.example.japanmap.domain.entity.TripPhoto
import java.util.UUID

/** Room 엔티티 ↔ Domain 변환. iOS `TripMapper` 대응. */
object TripMapper {

    fun toDomain(row: TripWithPhotos): Trip = Trip(
        id = UUID.fromString(row.trip.id),
        prefectureID = row.trip.prefectureID,
        subRegionID = row.trip.subRegionID,
        visitedAt = row.trip.visitedAt,
        endDate = row.trip.endDate,
        memo = row.trip.memo,
        photos = row.photos
            .sortedBy { it.createdAt }
            .map { TripPhoto(UUID.fromString(it.id), it.fileName, it.createdAt) },
    )

    fun toDbTrip(trip: Trip): TripDbEntity = TripDbEntity(
        id = trip.id.toString(),
        prefectureID = trip.prefectureID,
        subRegionID = trip.subRegionID,
        visitedAt = trip.visitedAt,
        endDate = trip.endDate,
        memo = trip.memo,
    )

    fun toDbPhotos(trip: Trip): List<PhotoDbEntity> = trip.photos.map {
        PhotoDbEntity(
            id = it.id.toString(),
            tripId = trip.id.toString(),
            fileName = it.fileName,
            createdAt = it.createdAt,
        )
    }
}
