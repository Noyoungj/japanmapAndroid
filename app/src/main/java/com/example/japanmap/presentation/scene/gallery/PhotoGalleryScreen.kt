package com.example.japanmap.presentation.scene.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.japanmap.di.AppContainer
import com.example.japanmap.domain.entity.TripPhoto
import androidx.compose.foundation.layout.size
import com.example.japanmap.presentation.designsystem.DesignColors
import com.example.japanmap.presentation.util.noRippleClickable
import java.util.UUID

/**
 * 여행 기록 사진 전체 화면 뷰어. iOS `PhotoGalleryViewController` 대응 (페이저 + 핀치줌).
 */
@Composable
fun PhotoGalleryScreen(
    container: AppContainer,
    tripId: String,
    startIndex: Int,
    onClose: () -> Unit,
) {
    var photos by remember { mutableStateOf<List<TripPhoto>>(emptyList()) }
    LaunchedEffect(tripId) {
        val trips = container.tripRepository.fetchAll()
        photos = trips.firstOrNull { it.id == UUID.fromString(tripId) }?.photos ?: emptyList()
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        if (photos.isNotEmpty()) {
            val pagerState = rememberPagerState(
                initialPage = startIndex.coerceIn(0, photos.size - 1),
                pageCount = { photos.size },
            )
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                var scale by remember { mutableStateOf(1f) }
                Box(
                    Modifier.fillMaxSize().pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ -> scale = (scale * zoom).coerceIn(1f, 4f) }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = container.photoStorage.fileFor(photos[page].fileName),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale),
                    )
                }
            }
            Text(
                "${pagerState.currentPage + 1} / ${photos.size}",
                color = DesignColors.Brand.primary,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
            )
        }
        Box(
            Modifier.align(Alignment.TopStart).padding(8.dp).size(48.dp).noRippleClickable { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, contentDescription = "닫기", tint = DesignColors.Brand.primary)
        }
    }
}
