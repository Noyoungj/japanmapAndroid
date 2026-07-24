package com.example.japanmap.data.local.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * SwiftData `TripEntity` 대응. cascade delete로 연관 사진 함께 삭제.
 */
@Entity(tableName = "trips")
data class TripDbEntity(
    @PrimaryKey val id: String,
    val prefectureID: Int,
    val subRegionID: String?,
    val visitedAt: Long,
    val endDate: Long?,
    val memo: String,
)

/**
 * SwiftData `PhotoEntity` 대응.
 */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = TripDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tripId")],
)
data class PhotoDbEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val fileName: String,
    val createdAt: Long,
)

/** Trip + 연관 사진 일괄 조회용. */
data class TripWithPhotos(
    @Embedded val trip: TripDbEntity,
    @Relation(parentColumn = "id", entityColumn = "tripId")
    val photos: List<PhotoDbEntity>,
)
