package com.example.japanmap.presentation.scene.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.PrefectureID
import com.example.japanmap.domain.repository.PrefectureRepository
import com.example.japanmap.domain.usecase.CalculateVisitProgressUseCase
import com.example.japanmap.presentation.designsystem.PrefectureColorMapper.MapMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 메인 화면 상태. iOS `MainViewModel` 포팅. */
data class MainUiState(
    val prefectures: List<Prefecture> = emptyList(),
    val visitedPrefectureIDs: Set<PrefectureID> = emptySet(),
    val selectedPrefectureID: PrefectureID? = null,
    val mode: MapMode = MapMode.ACTIVE,
    val visitedCount: Int = 0,
    val total: Int = 47,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class MainViewModel(
    private val prefectureRepository: PrefectureRepository,
    private val calculateVisitProgress: CalculateVisitProgressUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            runCatching { prefectureRepository.fetchAll() }
                .onSuccess { list -> _state.update { it.copy(prefectures = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = "도도부현 데이터를 불러오지 못했습니다. (${e.message})") } }
            refreshProgress()
        }
    }

    /** 상세/편집 화면에서 돌아왔을 때 호출. */
    fun refresh() {
        viewModelScope.launch { refreshProgress() }
    }

    private suspend fun refreshProgress() {
        runCatching { calculateVisitProgress() }
            .onSuccess { p ->
                _state.update { it.copy(visitedPrefectureIDs = p.visited, visitedCount = p.visitedCount, total = p.total) }
            }
            .onFailure { e -> _state.update { it.copy(error = "진행률을 계산하지 못했습니다. (${e.message})") } }
    }

    /** 지도 탭 — 토글/단일 선택 (iOS StateMachine.toggleSelection 대응). */
    fun onPrefectureTapped(id: PrefectureID) {
        _state.update {
            it.copy(selectedPrefectureID = if (it.selectedPrefectureID == id) null else id)
        }
    }

    fun clearSelection() = _state.update { it.copy(selectedPrefectureID = null) }
    fun consumeError() = _state.update { it.copy(error = null) }
}
