package com.example.japanmap.domain.entity

/**
 * 일본의 8개 지방(지역) 구분. iOS `Region.swift` 포팅.
 */
enum class Region(val key: String, val koreanName: String) {
    HOKKAIDO("hokkaido", "홋카이도"),
    TOHOKU("tohoku", "도호쿠"),
    KANTO("kanto", "간토"),
    CHUBU("chubu", "주부"),
    KANSAI("kansai", "간사이"),
    CHUGOKU("chugoku", "주고쿠"),
    SHIKOKU("shikoku", "시코쿠"),
    KYUSHU("kyushu", "규슈");

    companion object {
        fun fromKey(key: String): Region =
            entries.firstOrNull { it.key == key } ?: HOKKAIDO
    }
}
