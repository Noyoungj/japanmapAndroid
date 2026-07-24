package com.example.japanmap.presentation.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val full = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)   // iOS TripListCell 단일/시작
private val dayOnly = SimpleDateFormat("d일", Locale.KOREA)           // 같은 달 종료
private val monthDay = SimpleDateFormat("M월 d일", Locale.KOREA)      // 다른 달 종료

/**
 * 여행 날짜 표시. iOS `TripListCell` 포맷 재현.
 * - 단일: "yyyy년 M월 d일"
 * - 같은 달 기간: "yyyy년 M월 d일 ~ d일"
 * - 다른 달 기간: "yyyy년 M월 d일 ~ M월 d일"
 */
fun formatTripDate(start: Long, end: Long?): String {
    val s = full.format(Date(start))
    if (end == null || end == start) return s
    val cs = Calendar.getInstance().apply { timeInMillis = start }
    val ce = Calendar.getInstance().apply { timeInMillis = end }
    val sameMonth = cs.get(Calendar.YEAR) == ce.get(Calendar.YEAR) &&
        cs.get(Calendar.MONTH) == ce.get(Calendar.MONTH)
    return if (sameMonth) "$s ~ ${dayOnly.format(Date(end))}"
    else "$s ~ ${monthDay.format(Date(end))}"
}

/** 에디터 date chip 선택값 포맷. 단일 "yyyy년 M월 d일", 기간 "start  ~  end". */
fun formatEditorDate(start: Long, end: Long?): String {
    val s = full.format(Date(start))
    if (end == null || end == start) return s
    return "$s  ~  ${full.format(Date(end))}"
}
