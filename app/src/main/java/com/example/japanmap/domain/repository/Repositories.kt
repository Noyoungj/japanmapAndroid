package com.example.japanmap.domain.repository

import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.PrefectureID
import com.example.japanmap.domain.entity.SubRegion
import com.example.japanmap.domain.entity.Trip
import java.util.UUID

/** 47개 도도부현 정적 데이터 조회. iOS `PrefectureRepository` 포팅. */
interface PrefectureRepository {
    suspend fun fetchAll(): List<Prefecture>
    suspend fun fetch(id: PrefectureID): Prefecture?
}

/** 도도부현별 하위 지역 목록 제공. iOS `SubRegionRepository` 포팅. */
interface SubRegionRepository {
    suspend fun fetchSubRegions(prefectureID: PrefectureID): List<SubRegion>
}

/** 사용자의 여행 기록 CRUD. iOS `TripRepository` 포팅. */
interface TripRepository {
    suspend fun fetchAll(): List<Trip>
    suspend fun fetch(prefectureID: PrefectureID): List<Trip>
    suspend fun fetchVisitedPrefectureIDs(): Set<PrefectureID>
    suspend fun add(trip: Trip): Trip
    suspend fun update(trip: Trip)
    suspend fun delete(tripID: UUID)
}

/**
 * 사진 바이너리 파일 입출력. iOS `PhotoStorageRepository` 포팅.
 * Domain이 Bitmap/안드로이드 타입에 의존하지 않도록 ByteArray 단위로 다룬다.
 */
interface PhotoStorageRepository {
    /** 원본 사진 데이터 저장. 저장된 파일명을 반환. */
    suspend fun save(imageData: ByteArray): String
    /** 파일명으로 원본 데이터 로드. */
    suspend fun loadImageData(fileName: String): ByteArray
    /** 저장된 사진 파일의 절대 경로 (Coil 로딩용). */
    fun fileFor(fileName: String): java.io.File
    /** 사진 파일 삭제. */
    suspend fun delete(fileName: String)
}
