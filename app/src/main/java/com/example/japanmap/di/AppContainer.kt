package com.example.japanmap.di

import android.content.Context
import androidx.room.Room
import com.example.japanmap.data.local.db.AppDatabase
import com.example.japanmap.data.local.file.PhotoFileStorage
import com.example.japanmap.data.local.json.PrefectureLocalDataSource
import com.example.japanmap.data.local.json.SubRegionLocalDataSource
import com.example.japanmap.data.repository.PrefectureRepositoryImpl
import com.example.japanmap.data.repository.SubRegionRepositoryImpl
import com.example.japanmap.data.repository.TripRepositoryImpl
import com.example.japanmap.domain.repository.PhotoStorageRepository
import com.example.japanmap.domain.repository.PrefectureRepository
import com.example.japanmap.domain.repository.SubRegionRepository
import com.example.japanmap.domain.repository.TripRepository
import com.example.japanmap.domain.usecase.AddTripUseCase
import com.example.japanmap.domain.usecase.CalculateVisitProgressUseCase
import com.example.japanmap.domain.usecase.DeleteTripUseCase
import com.example.japanmap.domain.usecase.FetchTripsUseCase
import com.example.japanmap.domain.usecase.SavePhotoUseCase

/**
 * Composition root. iOS `DIContainer` 대응 — Domain 프로토콜과 Data 구현을 조립.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase = Room.databaseBuilder(
        appContext, AppDatabase::class.java, "japanmap.db",
    ).build()

    // Repositories
    val prefectureRepository: PrefectureRepository =
        PrefectureRepositoryImpl(PrefectureLocalDataSource(appContext))

    val subRegionRepository: SubRegionRepository =
        SubRegionRepositoryImpl(SubRegionLocalDataSource(appContext))

    val photoStorage: PhotoStorageRepository = PhotoFileStorage(appContext)

    val tripRepository: TripRepository =
        TripRepositoryImpl(database.tripDao(), photoStorage)

    // UseCases
    fun addTripUseCase() = AddTripUseCase(tripRepository)
    fun deleteTripUseCase() = DeleteTripUseCase(tripRepository)
    fun fetchTripsUseCase() = FetchTripsUseCase(tripRepository)
    fun calculateVisitProgressUseCase() = CalculateVisitProgressUseCase(tripRepository)
    fun savePhotoUseCase() = SavePhotoUseCase(photoStorage)
}
