package com.example.japanmap.presentation.scene.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.japanmap.di.AppContainer
import com.example.japanmap.presentation.ViewModelFactory
import com.example.japanmap.presentation.designsystem.DesignColors
import com.example.japanmap.presentation.util.formatEditorDate
import com.example.japanmap.presentation.util.noRippleClickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripEditorScreen(
    container: AppContainer,
    prefectureID: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TripEditorViewModel = viewModel(factory = ViewModelFactory(container, prefectureID)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showDestMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(TripEditorViewModel.MAX_PHOTO_COUNT),
    ) { uris: List<Uri> ->
        scope.launch {
            uris.forEach { uri ->
                val bytes = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
                if (bytes != null) viewModel.addPhoto(bytes)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(DesignColors.Paper.canvas)) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            // 커스텀 헤더
            Box(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    "${state.prefecture?.ko ?: ""}에서의 추억",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DesignColors.Brand.primary,
                    modifier = Modifier.align(Alignment.Center),
                )
                Box(
                    Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).size(32.dp).noRippleClickable { onBack() },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Close, contentDescription = "취소", tint = DesignColors.Brand.primary) }
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(Modifier.height(10.dp))
                // 1. 사진
                SectionHeader(if (state.pendingPhotos.isEmpty()) "추억을 업로드해주세요" else "추억 ${state.pendingPhotos.size}장")
                Row(
                    Modifier.fillMaxWidth().height(108.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.pendingPhotos.size < TripEditorViewModel.MAX_PHOTO_COUNT) {
                        AddPhotoCell {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                    state.pendingPhotos.forEachIndexed { index, pending ->
                        Box(Modifier.size(100.dp)) {
                            AsyncImage(
                                model = container.photoStorage.fileFor(pending.photo.fileName),
                                contentDescription = null, contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            )
                            Box(
                                Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp).clip(CircleShape)
                                    .background(DesignColors.Brand.primary.copy(alpha = 0.7f)).noRippleClickable { viewModel.removePhoto(index) },
                                contentAlignment = Alignment.Center,
                            ) { Icon(Icons.Filled.Close, "삭제", tint = Color.White, modifier = Modifier.size(12.dp)) }
                        }
                    }
                }

                // 2. 여행지 (subRegions 있을 때만)
                if (state.subRegions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader("어디를 다녀왔어요?")
                    Box {
                        ChipCard(onClick = { showDestMenu = true }) {
                            Icon(Icons.Filled.Place, null, tint = if (state.selectedSubRegionID != null) DesignColors.Brand.secondary else DesignColors.System.placeholderText, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                state.selectedSubRegionName ?: "여행지를 선택해주세요 (선택)",
                                fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                color = if (state.selectedSubRegionID != null) DesignColors.Brand.primary else DesignColors.System.placeholderText,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(Icons.Filled.UnfoldMore, null, tint = DesignColors.System.tertiaryLabel, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showDestMenu, onDismissRequest = { showDestMenu = false }) {
                            DropdownMenuItem(text = { Text("선택 안 함") }, onClick = { viewModel.selectSubRegion(null); showDestMenu = false })
                            state.subRegions.forEach { sr ->
                                DropdownMenuItem(text = { Text(sr.ko) }, onClick = { viewModel.selectSubRegion(sr.id); showDestMenu = false })
                            }
                        }
                    }
                }

                // 3. 날짜
                Spacer(Modifier.height(8.dp))
                SectionHeader("언제 다녀왔어요?")
                ChipCard(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = DesignColors.Brand.secondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(formatEditorDate(state.visitedAt, state.endDate), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DesignColors.Brand.primary)
                }

                // 4. 메모
                Spacer(Modifier.height(8.dp))
                SectionHeader("누구와의 추억인가요?")
                TextField(
                    value = state.memo,
                    onValueChange = viewModel::setMemo,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = DesignColors.Brand.primary, ambientColor = DesignColors.Brand.primary)
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = { Text("누구와의 추억인가요?", color = DesignColors.System.placeholderText, fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                Spacer(Modifier.height(16.dp))
            }

            // 하단 고정 저장 버튼
            Box(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                val enabled = state.canSave && !state.isSaving
                Box(
                    Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (enabled) DesignColors.Brand.secondary else DesignColors.Brand.secondary.copy(alpha = 0.4f))
                        .noRippleClickable(enabled = enabled) { viewModel.save() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("추억 등록", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }

    if (showDatePicker) {
        DateRangeDialog(
            initialStart = state.visitedAt, initialEnd = state.endDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e -> viewModel.setDates(s, e ?: s); showDatePicker = false },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DesignColors.Brand.primary)
}

@Composable
private fun ChipCard(onClick: () -> Unit, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(44.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = DesignColors.Brand.primary, ambientColor = DesignColors.Brand.primary)
            .clip(RoundedCornerShape(12.dp)).background(Color.White).noRippleClickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun AddPhotoCell(onClick: () -> Unit) {
    val stroke = DesignColors.Brand.secondary.copy(alpha = 0.45f)
    Box(
        Modifier.size(100.dp)
            .drawBehind {
                drawRoundRect(
                    color = stroke, style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                )
            }
            .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "사진 추가", tint = DesignColors.Brand.secondary, modifier = Modifier.size(24.dp)) }
}

@Composable
private fun DateRangeDialog(initialStart: Long, initialEnd: Long?, onDismiss: () -> Unit, onConfirm: (Long, Long?) -> Unit) {
    var sel by remember { mutableStateOf(initialStart to initialEnd) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.clip(RoundedCornerShape(24.dp)).background(DesignColors.Paper.canvas).padding(16.dp)) {
            // 헤더: 여행 기간 + X
            Box(Modifier.fillMaxWidth()) {
                Text("여행 기간", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DesignColors.Brand.primary, modifier = Modifier.align(Alignment.Center))
                Box(Modifier.align(Alignment.CenterEnd).size(32.dp).noRippleClickable { onDismiss() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Close, "닫기", tint = DesignColors.Brand.primary)
                }
            }
            Spacer(Modifier.height(24.dp))
            CustomCalendar(initialStart = initialStart, initialEnd = initialEnd, onChange = { s, e -> sel = s to e })
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp))
                    .background(DesignColors.Brand.secondary)
                    .noRippleClickable { val (s, e) = sel; onConfirm(s, e ?: s) },
                contentAlignment = Alignment.Center,
            ) { Text("선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
        }
    }
}
