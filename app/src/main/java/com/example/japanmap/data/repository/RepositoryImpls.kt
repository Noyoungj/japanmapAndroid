package com.example.japanmap.data.repository

import com.example.japanmap.data.local.db.TripDao
import com.example.japanmap.data.local.json.PrefectureLocalDataSource
import com.example.japanmap.data.local.json.SubRegionLocalDataSource
import com.example.japanmap.data.mapper.TripMapper
import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.PrefectureID
import com.example.japanmap.domain.entity.SubRegion
import com.example.japanmap.domain.entity.Trip
import com.example.japanmap.domain.repository.PhotoStorageRepository
import com.example.japanmap.domain.repository.PrefectureRepository
import com.example.japanmap.domain.repository.SubRegionRepository
import com.example.japanmap.domain.repository.TripRepository
import java.util.UUID

class PrefectureRepositoryImpl(
    private val dataSource: PrefectureLocalDataSource,
) : PrefectureRepository {
    override suspend fun fetchAll(): List<Prefecture> = dataSource.loadAll()
    override suspend fun fetch(id: PrefectureID): Prefecture? =
        dataSource.loadAll().firstOrNull { it.num == id }
}

class SubRegionRepositoryImpl(
    private val dataSource: SubRegionLocalDataSource,
) : SubRegionRepository {
    override suspend fun fetchSubRegions(prefectureID: PrefectureID): List<SubRegion> =
        dataSource.load(prefectureID)
}

class TripRepositoryImpl(
    private val dao: TripDao,
    private val photoStorage: PhotoStorageRepository,
) : TripRepository {

    override suspend fun fetchAll(): List<Trip> =
        dao.fetchAll().map(TripMapper::toDomain)

    override suspend fun fetch(prefectureID: PrefectureID): List<Trip> =
        dao.fetchByPrefecture(prefectureID).map(TripMapper::toDomain)

    override suspend fun fetchVisitedPrefectureIDs(): Set<PrefectureID> =
        dao.fetchVisitedPrefectureIDs().toSet()

    override suspend fun add(trip: Trip): Trip {
        dao.upsertTripWithPhotos(TripMapper.toDbTrip(trip), TripMapper.toDbPhotos(trip))
        return trip
    }

    override suspend fun update(trip: Trip) {
        dao.upsertTripWithPhotos(TripMapper.toDbTrip(trip), TripMapper.toDbPhotos(trip))
    }

    override suspend fun delete(tripID: UUID) {
        // 연관 사진 파일 정리 후 DB 삭제 (cascade가 photos 행 삭제).
        val row = dao.fetchById(tripID.toString())
        row?.photos?.forEach { photoStorage.delete(it.fileName) }
        dao.deleteTrip(tripID.toString())
    }
}
