package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DotCoral
import com.example.ui.theme.DotCyan
import com.example.ui.theme.DotGold
import com.example.ui.theme.DotMint
import com.example.ui.theme.DotPurple

enum class DotColor(val displayName: String, val composeColor: Color, val hex: String) {
    CORAL("Merah Karang", DotCoral, "#FF4757"),
    CYAN("Cyan Elektrik", DotCyan, "#00D2D3"),
    GOLD("Kuning Emas", DotGold, "#FFA502"),
    MINT("Hijau Mint", DotMint, "#2ED573"),
    PURPLE("Ungu Royal", DotPurple, "#8854D0");

    companion object {
        fun random(): DotColor = entries.random()
    }
}

data class DotCoordinate(val row: Int, val col: Int)

data class DotItem(
    val id: String,
    val row: Int,
    val col: Int,
    val color: DotColor,
    val isLoopTarget: Boolean = false,
    val isPopping: Boolean = false
)

enum class GameMode(
    val title: String,
    val subtitle: String,
    val iconDescription: String,
    val initialTimeSec: Int,
    val initialMoves: Int
) {
    TIMED("Waktu (60 Detik)", "Hubungkan titik sebanyak mungkin sebelum waktu habis!", "Timer", 60, -1),
    MOVES("Langkah (30 Gerakan)", "Maksimalkan skor dalam 30 kali penarikan garis!", "Moves", -1, 30),
    ZEN("Mode Zen Santai", "Bermain tanpa batas waktu atau langkah untuk relaksasi.", "Zen", -1, -1)
}

enum class PowerUpType(
    val title: String,
    val description: String,
    val defaultCount: Int
) {
    ERASER("Hapus Titik", "Ketuk satu titik untuk menghapusnya", 2),
    COLOR_BOMB("Bom Warna", "Hapus semua titik berwarna sama di papan", 1),
    SHUFFLE("Acak Ulang", "Acak posisi semua titik di papan", 2)
}

data class FloatingParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val alpha: Float,
    val size: Float
)

data class FloatingScoreText(
    val text: String,
    val x: Float,
    val y: Float,
    val color: Color,
    val alpha: Float = 1f
)
