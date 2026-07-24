package com.example.japanmap.presentation.scene.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.SubRegion
import com.example.japanmap.domain.entity.Trip
import com.example.japanmap.domain.repository.PrefectureRepository
import com.example.japanmap.domain.repository.SubRegionRepository
import com.example.japanmap.domain.usecase.DeleteTripUseCase
import com.example.japanmap.domain.usecase.FetchTripsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class DetailUiState(
    val prefecture: Prefecture? = null,
    val trips: List<Trip> = emptyList(),
    val subRegions: List<SubRegion> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** 도도부현 상세 화면. iOS `PrefectureDetailViewModel` 포팅. */
class PrefectureDetailViewModel(
    private val prefectureID: Int,
    private val prefectureRepository: PrefectureRepository,
    private val fetchTrips: FetchTripsUseCase,
    private val deleteTrip: DeleteTripUseCase,
    private val subRegionRepository: SubRegionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val p = prefectureRepository.fetch(prefectureID)
            val subs = subRegionRepository.fetchSubRegions(prefectureID)
            _state.update { it.copy(prefecture = p, subRegions = subs) }
            reload()
        }
    }

    fun reload() {
        viewModelScope.launch {
            runCatching { fetchTrips(prefectureID) }
                .onSuccess { list -> _state.update { it.copy(trips = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = "여행 기록을 불러오지 못했습니다. (${e.message})") } }
        }
    }

    fun subRegionName(trip: Trip): String? =
        trip.subRegionID?.let { id -> _state.value.subRegions.firstOrNull { it.id == id }?.ko }

    fun delete(tripID: UUID) {
        viewModelScope.launch {
            runCatching { deleteTrip(tripID) }
                .onSuccess { _state.update { s -> s.copy(trips = s.trips.filterNot { it.id == tripID }) } }
                .onFailure { e -> _state.update { it.copy(error = "삭제에 실패했습니다. (${e.message})") } }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }
}
