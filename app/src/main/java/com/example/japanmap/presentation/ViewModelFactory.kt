package com.example.japanmap.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.japanmap.di.AppContainer
import com.example.japanmap.presentation.scene.detail.PrefectureDetailViewModel
import com.example.japanmap.presentation.scene.editor.TripEditorViewModel
import com.example.japanmap.presentation.scene.main.MainViewModel

/**
 * 수동 DI ViewModel 팩토리. iOS Coordinator + DIContainer.make* 대응.
 */
class ViewModelFactory(
    private val container: AppContainer,
    private val prefectureID: Int = -1,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MainViewModel::class.java) ->
            MainViewModel(container.prefectureRepository, container.calculateVisitProgressUseCase()) as T

        modelClass.isAssignableFrom(PrefectureDetailViewModel::class.java) ->
            PrefectureDetailViewModel(
                prefectureID,
                container.prefectureRepository,
                container.fetchTripsUseCase(),
                container.deleteTripUseCase(),
                container.subRegionRepository,
            ) as T

        modelClass.isAssignableFrom(TripEditorViewModel::class.java) ->
            TripEditorViewModel(
                prefectureID,
                container.prefectureRepository,
                container.savePhotoUseCase(),
                container.addTripUseCase(),
                container.subRegionRepository,
            ) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
