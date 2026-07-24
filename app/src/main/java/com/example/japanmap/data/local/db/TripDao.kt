package com.example.japanmap.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TripDao {

    @Transaction
    @Query("SELECT * FROM trips ORDER BY visitedAt DESC")
    suspend fun fetchAll(): List<TripWithPhotos>

    @Transaction
    @Query("SELECT * FROM trips WHERE prefectureID = :prefectureID ORDER BY visitedAt DESC")
    suspend fun fetchByPrefecture(prefectureID: Int): List<TripWithPhotos>

    @Query("SELECT DISTINCT prefectureID FROM trips")
    suspend fun fetchVisitedPrefectureIDs(): List<Int>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun fetchById(id: String): TripWithPhotos?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripDbEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoDbEntity>)

    @Query("DELETE FROM photos WHERE tripId = :tripId")
    suspend fun deletePhotosOfTrip(tripId: String)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: String)

    @Transaction
    suspend fun upsertTripWithPhotos(trip: TripDbEntity, photos: List<PhotoDbEntity>) {
        insertTrip(trip)
        deletePhotosOfTrip(trip.id)
        insertPhotos(photos)
    }
}
