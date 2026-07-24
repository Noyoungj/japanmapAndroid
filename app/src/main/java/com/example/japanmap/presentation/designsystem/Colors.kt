package com.example.japanmap.presentation.designsystem

import androidx.compose.ui.graphics.Color

/**
 * 디자인 토큰 — iOS `Colors.swift` (`DesignColors`)와 1:1 매핑.
 * 모든 화면에서 컬러를 하드코딩하지 말고 본 객체를 통해 접근한다.
 */
object DesignColors {

    object Region {
        object Hokkaido {
            val default = Color(0xFFF4A6A6); val highlighted = Color(0xFFEC7878); val selected = Color(0xFFD94545)
        }
        object Tohoku {
            val default = Color(0xFFF7D87A); val highlighted = Color(0xFFF0C13D); val selected = Color(0xFFD9A210)
        }
        object Kanto {
            val default = Color(0xFF9FD89F); val highlighted = Color(0xFF6FC46F); val selected = Color(0xFF3FA13F)
        }
        object Chubu {
            val default = Color(0xFFA8D7E5); val highlighted = Color(0xFF6FBCD2); val selected = Color(0xFF2F94B3)
        }
        object Kansai {
            val default = Color(0xFF8FA8E0); val highlighted = Color(0xFF5C7FCC); val selected = Color(0xFF2E55B8)
        }
        object Chugoku {
            val default = Color(0xFFC5A8E0); val highlighted = Color(0xFF9A78CC); val selected = Color(0xFF6A45B8)
        }
        object Shikoku {
            val default = Color(0xFFE8B89F); val highlighted = Color(0xFFD89570); val selected = Color(0xFFB8662E)
        }
        object Kyushu {
            val default = Color(0xFF7DCFC4); val highlighted = Color(0xFF4DB5A6); val selected = Color(0xFF2A8C80)
        }
    }

    object Inactive {
        val hokkaido = Color(0xFFC9C9C9); val tohoku = Color(0xFFC2C2C2); val kanto = Color(0xFFBFBFBF)
        val chubu = Color(0xFFC5C5C5); val kansai = Color(0xFFBABABA); val chugoku = Color(0xFFC0C0C0)
        val shikoku = Color(0xFFC3C3C3); val kyushu = Color(0xFFBDBDBD)
    }

    object Background {
        val canvasActive = Color(0xFFFFFBF5)
        val canvasInactive = Color(0xFFF7F7F7)
    }

    object Stroke {
        val activeDefault = Color(0xFFFFFFFF)
        val activeSelected = Color(0xFF1A1A1A)
        val inactive = Color(0xFFE8E8E8)
    }

    object Label {
        val active = Color(0xFF1A1A1A)
        val inactive = Color(0xFF8A8A8A)
        val outline = Color(0xFFFFFFFF)
    }

    object Brand {
        val primary = Color(0xFF1F2A44)
        val secondary = Color(0xFFFF7A59)
    }

    object Paper {
        val canvas = Color(0xFFFFFBF5)
    }

    /** iOS UIColor 시맨틱 컬러 근사치 (라이트 모드 기준). */
    object System {
        val secondaryLabel = Color(0x993C3C43)   // .secondaryLabel
        val tertiaryLabel = Color(0x4D3C3C43)     // .tertiaryLabel
        val placeholderText = Color(0x4D3C3C43)   // .placeholderText
        val systemGray6 = Color(0xFFF2F2F7)       // .systemGray6
    }
}
