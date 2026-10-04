package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRecord
import com.example.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val progress: String
)

data class StatsUiState(
    val timedHighScore: Int = 0,
    val movesHighScore: Int = 0,
    val zenHighScore: Int = 0,
    val dabHighScore: Int = 0,
    val totalDotsCleared: Int = 0,
    val totalSquaresFormed: Int = 0,
    val totalGamesPlayed: Int = 0,
    val recentRecords: List<GameRecord> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

class StatsViewModel(
    application: Application,
    private val repository: GameRepository = GameRepository(AppDatabase.getDatabase(application).gameDao())
) : AndroidViewModel(application) {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.allRecords,
        repository.getHighScore("TIMED"),
        repository.getHighScore("MOVES"),
        repository.getHighScore("ZEN"),
        repository.getHighScore("DOTS_AND_BOXES")
    ) { records, timedBest, movesBest, zenBest, dabBest ->
        val totalDots = records.sumOf { it.dotsCleared }
        val totalSquares = records.sumOf { it.squaresFormed }
        val totalGames = records.size
        val maxSingleChain = records.maxOfOrNull { it.maxChain } ?: 0
        val dabWins = records.count { it.gameMode == "DOTS_AND_BOXES" && it.isWin }

        val achievements = listOf(
            Achievement(
                id = "first_game",
                title = "Langkah Pertama",
                description = "Mainkan permainan Dots pertamamu",
                isUnlocked = totalGames >= 1,
                progress = if (totalGames >= 1) "Selesai" else "0/1"
            ),
            Achievement(
                id = "dots_100",
                title = "Penghubung Handal",
                description = "Kumpulkan total 100 titik yang terhapus",
                isUnlocked = totalDots >= 100,
                progress = "$totalDots/100"
            ),
            Achievement(
                id = "dots_500",
                title = "Master Titik",
                description = "Kumpulkan total 500 titik yang terhapus",
                isUnlocked = totalDots >= 500,
                progress = "$totalDots/500"
            ),
            Achievement(
                id = "squares_5",
                title = "Pencipta Kotak",
                description = "Bentuk 5 putaran kotak / loop tertutup",
                isUnlocked = totalSquares >= 5,
                progress = "$totalSquares/5"
            ),
            Achievement(
                id = "chain_8",
                title = "Rantai Raksasa",
                description = "Hubungkan 8 atau lebih titik dalam satu tarikan garis",
                isUnlocked = maxSingleChain >= 8,
                progress = if (maxSingleChain >= 8) "Tercapai ($maxSingleChain)" else "Maks: $maxSingleChain/8"
            ),
            Achievement(
                id = "dab_win",
                title = "Juara Titik & Kotak",
                description = "Menangkan pertandingan Titik & Kotak melawan Komputer/Lawan",
                isUnlocked = dabWins >= 1,
                progress = "$dabWins Menang"
            )
        )

        StatsUiState(
            timedHighScore = timedBest ?: 0,
            movesHighScore = movesBest ?: 0,
            zenHighScore = zenBest ?: 0,
            dabHighScore = dabBest ?: 0,
            totalDotsCleared = totalDots,
            totalSquaresFormed = totalSquares,
            totalGamesPlayed = totalGames,
            recentRecords = records.take(15),
            achievements = achievements
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    fun clearStats() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
