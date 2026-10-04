package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.DotsSoundManager
import com.example.data.AppDatabase
import com.example.data.GameRecord
import com.example.data.GameRepository
import com.example.model.AIDifficulty
import com.example.model.DABBox
import com.example.model.DABEdge
import com.example.model.DABPlayer
import com.example.model.OpponentMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class DotsAndBoxesUiState(
    val gridSize: Int = 3, // 3x3 boxes (4x4 dots)
    val opponentMode: OpponentMode = OpponentMode.VS_AI,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    val horizontalEdges: Set<Pair<Int, Int>> = emptySet(), // (row, col)
    val verticalEdges: Set<Pair<Int, Int>> = emptySet(),   // (row, col)
    val boxes: Map<Pair<Int, Int>, DABPlayer> = emptyMap(), // (row, col) -> owner
    val currentPlayer: DABPlayer = DABPlayer.PLAYER_1,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val isGameOver: Boolean = false,
    val winner: DABPlayer? = null,
    val isDraw: Boolean = false,
    val isAiThinking: Boolean = false
)

class DotsAndBoxesViewModel(
    application: Application,
    private val repository: GameRepository = GameRepository(AppDatabase.getDatabase(application).gameDao()),
    val soundManager: DotsSoundManager = DotsSoundManager(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DotsAndBoxesUiState())
    val uiState: StateFlow<DotsAndBoxesUiState> = _uiState.asStateFlow()

    fun startNewGame(
        gridSize: Int = _uiState.value.gridSize,
        mode: OpponentMode = _uiState.value.opponentMode,
        difficulty: AIDifficulty = _uiState.value.aiDifficulty
    ) {
        _uiState.update {
            DotsAndBoxesUiState(
                gridSize = gridSize,
                opponentMode = mode,
                aiDifficulty = difficulty,
                horizontalEdges = emptySet(),
                verticalEdges = emptySet(),
                boxes = emptyMap(),
                currentPlayer = DABPlayer.PLAYER_1,
                player1Score = 0,
                player2Score = 0,
                isGameOver = false,
                winner = null,
                isDraw = false,
                isAiThinking = false
            )
        }
    }

    fun onEdgeTapped(isHorizontal: Boolean, row: Int, col: Int) {
        val state = _uiState.value
        if (state.isGameOver || state.isAiThinking) return
        if (state.opponentMode == OpponentMode.VS_AI && state.currentPlayer == DABPlayer.AI) return

        val alreadyTaken = if (isHorizontal) {
            state.horizontalEdges.contains(row to col)
        } else {
            state.verticalEdges.contains(row to col)
        }
        if (alreadyTaken) return

        applyMove(isHorizontal, row, col)
    }

    private fun applyMove(isHorizontal: Boolean, row: Int, col: Int) {
        val state = _uiState.value
        val newH = if (isHorizontal) state.horizontalEdges + (row to col) else state.horizontalEdges
        val newV = if (!isHorizontal) state.verticalEdges + (row to col) else state.verticalEdges

        soundManager.vibrateDotConnect()
        soundManager.playDotConnect(2)

        // Check if this move completes any box
        val completedBoxes = mutableListOf<Pair<Int, Int>>()
        val totalBoxes = state.gridSize * state.gridSize

        // A horizontal line at (row, col) can bound:
        // 1. Box above at (row - 1, col) if row > 0
        // 2. Box below at (row, col) if row < gridSize
        // A vertical line at (row, col) can bound:
        // 1. Box left at (row, col - 1) if col > 0
        // 2. Box right at (row, col) if col < gridSize
        val candidateBoxes = if (isHorizontal) {
            listOfNotNull(
                if (row > 0) (row - 1) to col else null,
                if (row < state.gridSize) row to col else null
            )
        } else {
            listOfNotNull(
                if (col > 0) row to (col - 1) else null,
                if (col < state.gridSize) row to col else null
            )
        }

        for ((bRow, bCol) in candidateBoxes) {
            if (!state.boxes.containsKey(bRow to bCol)) {
                if (isBoxCompleted(bRow, bCol, newH, newV)) {
                    completedBoxes.add(bRow to bCol)
                }
            }
        }

        val newBoxes = state.boxes.toMutableMap()
        for (b in completedBoxes) {
            newBoxes[b] = state.currentPlayer
        }

        val boxesClaimed = completedBoxes.size
        var p1Score = state.player1Score
        var p2Score = state.player2Score

        if (boxesClaimed > 0) {
            soundManager.playLoopCelebration()
            soundManager.vibrateBoxClaimed()
            if (state.currentPlayer == DABPlayer.PLAYER_1) {
                p1Score += boxesClaimed
            } else {
                p2Score += boxesClaimed
            }
        }

        val allBoxesFilled = newBoxes.size == totalBoxes

        if (allBoxesFilled) {
            // Game Over!
            val winner = when {
                p1Score > p2Score -> DABPlayer.PLAYER_1
                p2Score > p1Score -> if (state.opponentMode == OpponentMode.VS_AI) DABPlayer.AI else DABPlayer.PLAYER_2
                else -> null
            }
            val isDraw = p1Score == p2Score

            _uiState.update {
                it.copy(
                    horizontalEdges = newH,
                    verticalEdges = newV,
                    boxes = newBoxes,
                    player1Score = p1Score,
                    player2Score = p2Score,
                    isGameOver = true,
                    winner = winner,
                    isDraw = isDraw,
                    isAiThinking = false
                )
            }

            // Save record to DB
            viewModelScope.launch {
                repository.saveRecord(
                    GameRecord(
                        gameMode = "DOTS_AND_BOXES",
                        score = p1Score,
                        dotsCleared = (newH.size + newV.size),
                        squaresFormed = p1Score,
                        isWin = winner == DABPlayer.PLAYER_1
                    )
                )
            }
            return
        }

        // Extra turn rule: If player claimed at least one box, they KEEP their turn!
        val nextPlayer = if (boxesClaimed > 0) {
            state.currentPlayer
        } else {
            when (state.currentPlayer) {
                DABPlayer.PLAYER_1 -> if (state.opponentMode == OpponentMode.VS_AI) DABPlayer.AI else DABPlayer.PLAYER_2
                DABPlayer.PLAYER_2 -> DABPlayer.PLAYER_1
                DABPlayer.AI -> DABPlayer.PLAYER_1
            }
        }

        _uiState.update {
            it.copy(
                horizontalEdges = newH,
                verticalEdges = newV,
                boxes = newBoxes,
                player1Score = p1Score,
                player2Score = p2Score,
                currentPlayer = nextPlayer
            )
        }

        if (state.opponentMode == OpponentMode.VS_AI && nextPlayer == DABPlayer.AI) {
            triggerAiMove()
        }
    }

    private fun isBoxCompleted(
        row: Int,
        col: Int,
        hEdges: Set<Pair<Int, Int>>,
        vEdges: Set<Pair<Int, Int>>
    ): Boolean {
        val top = hEdges.contains(row to col)
        val bottom = hEdges.contains((row + 1) to col)
        val left = vEdges.contains(row to col)
        val right = vEdges.contains(row to (col + 1))
        return top && bottom && left && right
    }

    private fun countEdgesOfBox(
        row: Int,
        col: Int,
        hEdges: Set<Pair<Int, Int>>,
        vEdges: Set<Pair<Int, Int>>
    ): Int {
        var count = 0
        if (hEdges.contains(row to col)) count++
        if (hEdges.contains((row + 1) to col)) count++
        if (vEdges.contains(row to col)) count++
        if (vEdges.contains(row to (col + 1))) count++
        return count
    }

    private fun triggerAiMove() {
        _uiState.update { it.copy(isAiThinking = true) }
        viewModelScope.launch {
            delay(550) // AI think time for natural feel
            val state = _uiState.value
            if (state.isGameOver || state.currentPlayer != DABPlayer.AI) {
                _uiState.update { it.copy(isAiThinking = false) }
                return@launch
            }

            val chosenMove = calculateBestAiMove(state)
            _uiState.update { it.copy(isAiThinking = false) }
            if (chosenMove != null) {
                applyMove(chosenMove.isHorizontal, chosenMove.row, chosenMove.col)
            }
        }
    }

    private fun calculateBestAiMove(state: DotsAndBoxesUiState): DABEdge? {
        val g = state.gridSize
        val availableEdges = mutableListOf<DABEdge>()

        // Horizontal edges: (0..g) rows x (0 until g) cols
        for (r in 0..g) {
            for (c in 0 until g) {
                if (!state.horizontalEdges.contains(r to c)) {
                    availableEdges.add(DABEdge(isHorizontal = true, row = r, col = c))
                }
            }
        }

        // Vertical edges: (0 until g) rows x (0..g) cols
        for (r in 0 until g) {
            for (c in 0..g) {
                if (!state.verticalEdges.contains(r to c)) {
                    availableEdges.add(DABEdge(isHorizontal = false, row = r, col = c))
                }
            }
        }

        if (availableEdges.isEmpty()) return null

        // 1. Check for immediate completing moves (box with 3 edges already)
        val completingMoves = availableEdges.filter { edge ->
            val testH = if (edge.isHorizontal) state.horizontalEdges + (edge.row to edge.col) else state.horizontalEdges
            val testV = if (!edge.isHorizontal) state.verticalEdges + (edge.row to edge.col) else state.verticalEdges
            val adj = getAdjacentBoxes(edge, g)
            adj.any { (br, bc) -> isBoxCompleted(br, bc, testH, testV) }
        }

        if (completingMoves.isNotEmpty()) {
            return completingMoves.random()
        }

        if (state.aiDifficulty == AIDifficulty.EASY) {
            return availableEdges.random()
        }

        // 2. Safe moves: moves that DO NOT give the opponent a 3rd edge (i.e. edges where adjacent boxes will have at most 2 edges)
        val safeMoves = availableEdges.filter { edge ->
            val testH = if (edge.isHorizontal) state.horizontalEdges + (edge.row to edge.col) else state.horizontalEdges
            val testV = if (!edge.isHorizontal) state.verticalEdges + (edge.row to edge.col) else state.verticalEdges
            val adj = getAdjacentBoxes(edge, g)
            adj.all { (br, bc) -> countEdgesOfBox(br, bc, testH, testV) < 3 }
        }

        if (safeMoves.isNotEmpty()) {
            return safeMoves.random()
        }

        // 3. If no safe moves, pick a move that gives away the minimum sacrifice
        return availableEdges.minByOrNull { edge ->
            val testH = if (edge.isHorizontal) state.horizontalEdges + (edge.row to edge.col) else state.horizontalEdges
            val testV = if (!edge.isHorizontal) state.verticalEdges + (edge.row to edge.col) else state.verticalEdges
            val adj = getAdjacentBoxes(edge, g)
            adj.count { (br, bc) -> countEdgesOfBox(br, bc, testH, testV) == 3 }
        } ?: availableEdges.random()
    }

    private fun getAdjacentBoxes(edge: DABEdge, gridSize: Int): List<Pair<Int, Int>> {
        return if (edge.isHorizontal) {
            listOfNotNull(
                if (edge.row > 0) (edge.row - 1) to edge.col else null,
                if (edge.row < gridSize) edge.row to edge.col else null
            )
        } else {
            listOfNotNull(
                if (edge.col > 0) edge.row to (edge.col - 1) else null,
                if (edge.col < gridSize) edge.row to edge.col else null
            )
        }
    }
}
