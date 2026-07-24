package com.example.japanmap.presentation.scene.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.japanmap.domain.entity.Prefecture
import com.example.japanmap.domain.entity.SubRegion
import com.example.japanmap.domain.entity.Trip
import com.example.japanmap.domain.entity.TripPhoto
import com.example.japanmap.domain.repository.PrefectureRepository
import com.example.japanmap.domain.repository.SubRegionRepository
import com.example.japanmap.domain.usecase.AddTripUseCase
import com.example.japanmap.domain.usecase.SavePhotoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PendingPhoto(val photo: TripPhoto)

data class EditorUiState(
    val prefecture: Prefecture? = null,
    val subRegions: List<SubRegion> = emptyList(),
    val selectedSubRegionID: String? = null,
    val visitedAt: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val memo: String = "",
    val pendingPhotos: List<PendingPhoto> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
) {
    /** 사진 1장 이상 또는 메모 한 글자 이상. iOS canSave 동일. */
    val canSave: Boolean get() = pendingPhotos.isNotEmpty() || memo.trim().isNotEmpty()
    val selectedSubRegionName: String?
        get() = selectedSubRegionID?.let { id -> subRegions.firstOrNull { it.id == id }?.ko }
}

/** 새 여행 기록 작성 화면. iOS `TripEditorViewModel` 포팅. */
class TripEditorViewModel(
    private val prefectureID: Int,
    private val prefectureRepository: PrefectureRepository,
    private val savePhoto: SavePhotoUseCase,
    private val addTrip: AddTripUseCase,
    private val subRegionRepository: SubRegionRepository,
) : ViewModel() {

    companion object { const val MAX_PHOTO_COUNT = 30 }

    private val _state = MutableStateFlow(EditorUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val p = prefectureRepository.fetch(prefectureID)
            val subs = subRegionRepository.fetchSubRegions(prefectureID)
            _state.update { it.copy(prefecture = p, subRegions = subs) }
        }
    }

    fun setMemo(value: String) = _state.update { it.copy(memo = value) }
    fun setDates(start: Long, end: Long) = _state.update { it.copy(visitedAt = start, endDate = end) }
    fun selectSubRegion(id: String?) = _state.update { it.copy(selectedSubRegionID = id) }

    /** 이미지 바이트를 추가 — 즉시 파일 저장 후 보류 목록에 추가. */
    fun addPhoto(imageData: ByteArray) {
        if (_state.value.pendingPhotos.size >= MAX_PHOTO_COUNT) {
            _state.update { it.copy(error = "최대 ${MAX_PHOTO_COUNT}장까지 첨부할 수 있어요.") }
            return
        }
        viewModelScope.launch {
            runCatching { savePhoto(imageData) }
                .onSuccess { photo -> _state.update { it.copy(pendingPhotos = it.pendingPhotos + PendingPhoto(photo)) } }
                .onFailure { e -> _state.update { it.copy(error = "사진 저장 실패 (${e.message})") } }
        }
    }

    fun removePhoto(index: Int) = _state.update {
        if (index in it.pendingPhotos.indices) it.copy(pendingPhotos = it.pendingPhotos.filterIndexed { i, _ -> i != index })
        else it
    }

    fun save() {
        val s = _state.value
        if (s.isSaving || !s.canSave) return
        _state.update { it.copy(isSaving = true) }
        val trip = Trip(
            id = UUID.randomUUID(),
            prefectureID = prefectureID,
            subRegionID = s.selectedSubRegionID,
            visitedAt = s.visitedAt,
            endDate = s.endDate,
            memo = s.memo,
            photos = s.pendingPhotos.map { it.photo },
        )
        viewModelScope.launch {
            runCatching { addTrip(trip) }
                .onSuccess { _state.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = "저장에 실패했습니다. (${e.message})") } }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }
}
