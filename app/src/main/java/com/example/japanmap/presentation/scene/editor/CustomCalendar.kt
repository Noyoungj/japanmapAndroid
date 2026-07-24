package com.example.japanmap.presentation.scene.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.japanmap.presentation.designsystem.DesignColors
import com.example.japanmap.presentation.util.noRippleClickable
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private val SYSTEM_RED = Color(0xFFFF3B30)

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * iOS `CustomCalendarView` 재현 — 월요일 시작, range를 연속 막대로 표시.
 */
@Composable
fun CustomCalendar(
    initialStart: Long,
    initialEnd: Long?,
    onChange: (start: Long, end: Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now() }
    var start by remember { mutableStateOf(initialStart.toLocalDate()) }
    var end by remember { mutableStateOf(initialEnd?.toLocalDate()) }
    var visibleMonth by remember { mutableStateOf(YearMonth.from(start)) }

    fun emit() = onChange(start.toEpochMillis(), end?.toEpochMillis())

    Column(modifier.fillMaxWidth()) {
        // 헤더: < yyyy년 M월 >
        Row(
            Modifier.fillMaxWidth().height(32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(32.dp).noRippleClickable { visibleMonth = visibleMonth.minusMonths(1) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ChevronLeft, "이전 달", tint = DesignColors.Brand.primary)
            }
            Spacer(Modifier.width(16.dp))
            androidx.compose.material3.Text(
                "${visibleMonth.year}년 ${visibleMonth.monthValue}월",
                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DesignColors.Brand.primary,
            )
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier.size(32.dp).noRippleClickable {
                    val next = visibleMonth.plusMonths(1)
                    if (!next.isAfter(YearMonth.from(today))) visibleMonth = next
                },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.ChevronRight, "다음 달", tint = DesignColors.Brand.primary) }
        }

        Spacer(Modifier.height(12.dp))

        // 요일 헤더 (월~일)
        Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEachIndexed { idx, w ->
                androidx.compose.material3.Text(
                    w, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = if (idx == 6) SYSTEM_RED.copy(alpha = 0.7f) else DesignColors.System.secondaryLabel,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // 그리드 42칸 (6주 × 7일), 월요일 시작
        val firstOfMonth = visibleMonth.atDay(1)
        val leadingOffset = firstOfMonth.dayOfWeek.value - 1 // MON=1 → 0
        val gridStart = firstOfMonth.minusDays(leadingOffset.toLong())
        val dates = (0 until 42).map { gridStart.plusDays(it.toLong()) }

        Column {
            for (week in 0 until 6) {
                Row(Modifier.fillMaxWidth()) {
                    for (dow in 0 until 7) {
                        val date = dates[week * 7 + dow]
                        DayCell(
                            date = date,
                            inMonth = YearMonth.from(date) == visibleMonth,
                            isFuture = date.isAfter(today),
                            start = start, end = end,
                            modifier = Modifier.weight(1f),
                            onTap = {
                                if (YearMonth.from(date) == visibleMonth && !date.isAfter(today)) {
                                    when {
                                        end != null -> { start = date; end = null }
                                        date.isBefore(start) -> start = date
                                        date == start -> end = date
                                        else -> end = date
                                    }
                                    emit()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isFuture: Boolean,
    start: LocalDate,
    end: LocalDate?,
    modifier: Modifier,
    onTap: () -> Unit,
) {
    val lower = if (end != null) minOf(start, end) else start
    val upper = if (end != null) maxOf(start, end) else start
    val inRange = inMonth && !isFuture && !date.isBefore(lower) && !date.isAfter(upper)
    val isStart = date == start
    val isEnd = end != null && date == end
    val isSingle = isStart && (end == null || isEnd)
    val isSunday = date.dayOfWeek.value == 7

    val rangeFill = DesignColors.Brand.secondary.copy(alpha = 0.22f)
    val selectedFill = DesignColors.Brand.secondary

    Box(modifier.height(44.dp), contentAlignment = Alignment.Center) {
        // range 막대 배경
        if (inRange && !isSingle) {
            val barShape = when {
                isStart -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                isEnd -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                else -> RoundedCornerShape(0)
            }
            Box(Modifier.fillMaxWidth().height(32.dp).clip(barShape).background(rangeFill))
        }
        // 선택 동그라미
        if (isStart || isEnd || isSingle) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(selectedFill))
        }
        // 날짜 숫자
        val textColor = when {
            !inMonth -> DesignColors.System.tertiaryLabel.copy(alpha = 0.5f)
            isFuture -> DesignColors.System.tertiaryLabel
            isStart || isEnd || isSingle -> Color.White
            isSunday -> SYSTEM_RED.copy(alpha = 0.85f)
            else -> DesignColors.Brand.primary
        }
        androidx.compose.material3.Text(
            "${date.dayOfMonth}",
            fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor,
            modifier = Modifier.noRippleClickable(enabled = inMonth && !isFuture) { onTap() }.padding(6.dp),
        )
    }
}
