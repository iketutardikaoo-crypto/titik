package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player2Color

enum class DABPlayer(val displayName: String, val color: Color, val tag: String) {
    PLAYER_1("Pemain 1 (Biru)", Player1Color, "P1"),
    PLAYER_2("Pemain 2 (Merah)", Player2Color, "P2"),
    AI("Komputer (Merah)", Player2Color, "BOT")
}

enum class OpponentMode(val title: String, val description: String) {
    VS_AI("Lawan Komputer", "Bermain solo melawan bot kecerdasan buatan"),
    PASS_AND_PLAY("2 Pemain (Lokal)", "Bermain bergantian dengan teman di satu layar")
}

enum class AIDifficulty(val title: String) {
    EASY("Santai"),
    MEDIUM("Menengah"),
    SMART("Pintar")
}

data class DABEdge(
    val isHorizontal: Boolean,
    val row: Int,
    val col: Int
)

data class DABBox(
    val row: Int,
    val col: Int,
    val owner: DABPlayer? = null
)
