package com.example.japanmap.presentation.scene.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.japanmap.di.AppContainer
import com.example.japanmap.presentation.ViewModelFactory
import com.example.japanmap.presentation.designsystem.DesignColors
import com.example.japanmap.presentation.map.JapanMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    container: AppContainer,
    onPrefectureSelected: (Int) -> Unit,
    viewModel: MainViewModel = viewModel(factory = ViewModelFactory(container)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("일본에서의 추억", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignColors.Paper.canvas,
                    titleContentColor = DesignColors.Brand.primary,
                ),
            )
        },
        containerColor = DesignColors.Paper.canvas,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.fillMaxSize().weight(1f)) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    JapanMap(
                        prefectures = state.prefectures,
                        visitedPrefectureIDs = state.visitedPrefectureIDs,
                        selectedPrefectureID = state.selectedPrefectureID,
                        mode = state.mode,
                        onPrefectureTapped = { id ->
                            viewModel.onPrefectureTapped(id)
                            onPrefectureSelected(id)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            ProgressCard(
                visited = state.visitedCount,
                total = state.total,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}

/** iOS `ProgressIndicatorView` 재현 — 하단 흰색 카드. */
@Composable
private fun ProgressCard(visited: Int, total: Int, modifier: Modifier = Modifier) {
    val ratio = if (total > 0) visited.toFloat() / total else 0f
    val percent = (ratio * 100).toInt()

    Column(
        modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = DesignColors.Brand.primary,
                spotColor = DesignColors.Brand.primary,
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // header row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("방문한 일본 지역", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DesignColors.System.secondaryLabel)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = DesignColors.Brand.secondary, modifier = Modifier.size(14.dp))
                Text("$percent%", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DesignColors.Brand.secondary)
            }
        }
        // number row (baseline)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$visited", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = DesignColors.Brand.primary)
            Spacer(Modifier.width(6.dp))
            Text("/ $total", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = DesignColors.System.secondaryLabel, modifier = Modifier.padding(bottom = 4.dp))
        }
        // progress bar (height 12, corner 6)
        Box(
            Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(DesignColors.System.systemGray6),
        ) {
            Box(Modifier.fillMaxWidth(ratio.coerceIn(0f, 1f)).height(12.dp).clip(RoundedCornerShape(6.dp)).background(DesignColors.Brand.secondary))
        }
    }
}
