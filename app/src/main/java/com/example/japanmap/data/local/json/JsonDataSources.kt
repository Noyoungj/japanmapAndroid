package com.example.japanmap.data.local.json

import android.content.Context
import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.PrefectureID
import com.example.japanmap.domain.entity.Region
import com.example.japanmap.domain.entity.SubRegion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// MARK: - DTO (JSON 스키마와 1:1)

@Serializable
private data class CentroidDto(val cx: Double, val cy: Double)

@Serializable
private data class PrefectureDto(
    val num: Int,
    val ko: String,
    val en: String,
    val region: String,
    val centroid: CentroidDto,
    val path: String,
)

@Serializable
private data class CoordinateDto(val lat: Double, val lon: Double)

@Serializable
private data class SubRegionDto(
    val id: String,
    val ko: String,
    val centroid: CentroidDto? = null,
    val coordinate: CoordinateDto? = null,
    val path: String? = null,
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * `assets/prefectures.json`에서 47개 도도부현을 로드. iOS `PrefectureLocalDataSource` 대응.
 * 최초 로드 후 메모리 캐싱.
 */
class PrefectureLocalDataSource(private val context: Context) {

    @Volatile private var cache: List<Prefecture>? = null

    suspend fun loadAll(): List<Prefecture> {
        cache?.let { return it }
        val text = context.assets.open("prefectures.json").bufferedReader().use { it.readText() }
        val dtos = json.decodeFromString<List<PrefectureDto>>(text)
        val result = dtos.map { dto ->
            Prefecture(
                num = dto.num,
                ko = dto.ko,
                en = dto.en,
                region = Region.fromKey(dto.region),
                centroid = Prefecture.Centroid(dto.centroid.cx, dto.centroid.cy),
                path = dto.path,
            )
        }.sortedBy { it.num }
        cache = result
        return result
    }
}

/**
 * `assets/subregions/{num}.json`에서 도도부현별 하위 지역 로드.
 * iOS `SubRegionLocalDataSource` 대응.
 */
class SubRegionLocalDataSource(private val context: Context) {

    private val cache = mutableMapOf<PrefectureID, List<SubRegion>>()

    suspend fun load(prefectureID: PrefectureID): List<SubRegion> {
        cache[prefectureID]?.let { return it }
        val result = runCatching {
            val text = context.assets.open("subregions/$prefectureID.json")
                .bufferedReader().use { it.readText() }
            json.decodeFromString<List<SubRegionDto>>(text).map { dto ->
                SubRegion(
                    id = dto.id,
                    ko = dto.ko,
                    centroid = dto.centroid?.let { SubRegion.Centroid(it.cx, it.cy) },
                    coordinate = dto.coordinate?.let { SubRegion.Coordinate(it.lat, it.lon) },
                    path = dto.path,
                )
            }
        }.getOrDefault(emptyList())
        cache[prefectureID] = result
        return result
    }
}
