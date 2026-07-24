package com.example.japanmap.presentation.scene.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.japanmap.di.AppContainer
import com.example.japanmap.domain.entity.Trip
import com.example.japanmap.presentation.ViewModelFactory
import com.example.japanmap.presentation.designsystem.DesignColors
import com.example.japanmap.presentation.map.PrefectureShapeCard
import com.example.japanmap.presentation.util.formatTripDate
import com.example.japanmap.presentation.util.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefectureDetailScreen(
    container: AppContainer,
    prefectureID: Int,
    onBack: () -> Unit,
    onAddTrip: () -> Unit,
    onOpenGallery: (tripId: String, startIndex: Int) -> Unit,
    viewModel: PrefectureDetailViewModel = viewModel(factory = ViewModelFactory(container, prefectureID)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title = state.prefecture?.ko ?: ""
    val visitedSubRegionIds = state.trips.mapNotNull { it.subRegionID }.toSet()
    val visitedSubRegions = state.subRegions.filter { it.id in visitedSubRegionIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = DesignColors.Brand.primary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddTrip) {
                        Icon(Icons.Filled.Add, contentDescription = "여행 기록 추가", tint = DesignColors.Brand.secondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignColors.Paper.canvas,
                    titleContentColor = DesignColors.Brand.primary,
                ),
            )
        },
        containerColor = DesignColors.Paper.canvas,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // 상단 지역 지도 (height 220, 좌우 16, 위 8)
            state.prefecture?.let { p ->
                Box(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        .fillMaxWidth()
                        .height(220.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = DesignColors.Brand.primary, ambientColor = DesignColors.Brand.primary)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                ) {
                    PrefectureShapeCard(prefecture = p, region = p.region, visitedSubRegions = visitedSubRegions)
                }
            }

            if (state.trips.isEmpty() && !state.isLoading) {
                EmptyTrips(Modifier.fillMaxSize(), onAddTrip)
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                ) {
                    items(state.trips, key = { it.id }) { trip ->
                        SwipeableTripRow(
                            trip = trip,
                            subRegionName = viewModel.subRegionName(trip),
                            fileFor = { fn -> container.photoStorage.fileFor(fn) },
                            onDelete = { viewModel.delete(trip.id) },
                            onOpen = { onOpenGallery(trip.id.toString(), 0) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTripRow(
    trip: Trip,
    subRegionName: String?,
    fileFor: (String) -> java.io.File,
    onDelete: () -> Unit,
    onOpen: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp)).background(Color(0xFFD94545)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text("삭제", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 28.dp))
            }
        },
    ) {
        TripCard(trip, subRegionName, fileFor, onOpen)
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    subRegionName: String?,
    fileFor: (String) -> java.io.File,
    onOpen: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = DesignColors.Brand.primary, ambientColor = DesignColors.Brand.primary)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .noRippleClickable { onOpen() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(DesignColors.System.systemGray6), contentAlignment = Alignment.Center) {
            val first = trip.photos.firstOrNull()
            if (first != null) {
                AsyncImage(model = fileFor(first.fileName), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Filled.Photo, contentDescription = null, tint = DesignColors.System.tertiaryLabel, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (subRegionName != null) {
                Text("📍 $subRegionName", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DesignColors.Brand.secondary)
            }
            Text(formatTripDate(trip.visitedAt, trip.endDate), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DesignColors.Brand.primary)
            Text(
                trip.memo.ifBlank { "메모 없음" },
                fontSize = 13.sp,
                color = DesignColors.System.secondaryLabel,
                maxLines = 2,
            )
        }
        Spacer(Modifier.size(10.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = DesignColors.Brand.secondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyTrips(modifier: Modifier, onAddTrip: () -> Unit) {
    Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.MenuBook, contentDescription = null, tint = DesignColors.Brand.secondary.copy(alpha = 0.7f), modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(12.dp))
        Text("아직 빈 페이지예요", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = DesignColors.Brand.primary)
        Spacer(Modifier.height(8.dp))
        Text(
            "오른쪽 위 + 버튼을 눌러\n첫 추억을 남겨보세요.",
            fontSize = 14.sp,
            color = DesignColors.System.secondaryLabel,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        androidx.compose.material3.Button(
            onClick = onAddTrip,
            shape = RoundedCornerShape(50),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DesignColors.Brand.secondary),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("여행 기록 추가", fontWeight = FontWeight.SemiBold)
        }
    }
}
