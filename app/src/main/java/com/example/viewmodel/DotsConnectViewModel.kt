package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.DotsSoundManager
import com.example.data.AppDatabase
import com.example.data.GameRecord
import com.example.data.GameRepository
import com.example.model.DotColor
import com.example.model.DotCoordinate
import com.example.model.DotItem
import com.example.model.FloatingParticle
import com.example.model.FloatingScoreText
import com.example.model.GameMode
import com.example.model.PowerUpType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

data class DotsConnectUiState(
    val gameMode: GameMode = GameMode.TIMED,
    val board: List<List<DotItem>> = emptyList(),
    val currentChain: List<DotCoordinate> = emptyList(),
    val isLoopFormed: Boolean = false,
    val score: Int = 0,
    val movesLeft: Int = 30,
    val timeLeftSec: Int = 60,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val dotsCleared: Int = 0,
    val squaresFormed: Int = 0,
    val maxChainLength: Int = 0,
    val bestScore: Int = 0,
    val activePowerUp: PowerUpType? = null,
    val erasersLeft: Int = 2,
    val colorBombsLeft: Int = 1,
    val shufflesLeft: Int = 2,
    val particles: List<FloatingParticle> = emptyList(),
    val floatingScores: List<FloatingScoreText> = emptyList()
)

class DotsConnectViewModel(
    application: Application,
    private val repository: GameRepository = GameRepository(AppDatabase.getDatabase(application).gameDao()),
    val soundManager: DotsSoundManager = DotsSoundManager(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DotsConnectUiState())
    val uiState: StateFlow<DotsConnectUiState> = _uiState.asStateFlow()

    private val rows = 6
    private val cols = 6
    private var timerJob: Job? = null

    init {
        loadBestScore(GameMode.TIMED)
    }

    fun startNewGame(mode: GameMode) {
        timerJob?.cancel()
        loadBestScore(mode)

        val newBoard = generateInitialBoard()
        _uiState.update {
            it.copy(
                gameMode = mode,
                board = newBoard,
                currentChain = emptyList(),
                isLoopFormed = false,
                score = 0,
                movesLeft = if (mode == GameMode.MOVES) mode.initialMoves else -1,
                timeLeftSec = if (mode == GameMode.TIMED) mode.initialTimeSec else -1,
                isPlaying = true,
                isPaused = false,
                isGameOver = false,
                dotsCleared = 0,
                squaresFormed = 0,
                maxChainLength = 0,
                activePowerUp = null,
                erasersLeft = PowerUpType.ERASER.defaultCount,
                colorBombsLeft = PowerUpType.COLOR_BOMB.defaultCount,
                shufflesLeft = PowerUpType.SHUFFLE.defaultCount,
                particles = emptyList(),
                floatingScores = emptyList()
            )
        }

        if (mode == GameMode.TIMED) {
            startTimer()
        }
    }

    private fun loadBestScore(mode: GameMode) {
        viewModelScope.launch {
            repository.getHighScore(mode.name).collect { best ->
                _uiState.update { it.copy(bestScore = best ?: 0) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeftSec > 0 && !_uiState.value.isGameOver) {
                delay(1000)
                if (!_uiState.value.isPaused && _uiState.value.isPlaying) {
                    val remaining = _uiState.value.timeLeftSec - 1
                    _uiState.update { it.copy(timeLeftSec = remaining) }
                    if (remaining <= 0) {
                        endGame()
                    }
                }
            }
        }
    }

    fun togglePause() {
        if (!_uiState.value.isPlaying || _uiState.value.isGameOver) return
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    // Touch gesture handling
    fun onDotTouchStart(row: Int, col: Int) {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver) return

        // Check if in Eraser power-up mode
        if (state.activePowerUp == PowerUpType.ERASER) {
            applyEraser(row, col)
            return
        }

        val dot = state.board.getOrNull(row)?.getOrNull(col) ?: return
        _uiState.update {
            it.copy(
                currentChain = listOf(DotCoordinate(row, col)),
                isLoopFormed = false
            )
        }
        soundManager.playDotConnect(0)
        soundManager.vibrateDotConnect()
    }

    fun onDotTouchDrag(row: Int, col: Int) {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver) return
        if (state.activePowerUp != null) return

        val currentChain = state.currentChain
        if (currentChain.isEmpty()) return

        val last = currentChain.last()
        if (last.row == row && last.col == col) return // Still on same dot

        // Check adjacency (horizontal or vertical only)
        val isAdjacent = (abs(last.row - row) + abs(last.col - col)) == 1
        if (!isAdjacent) return

        val firstDotCoord = currentChain.first()
        val chainColor = state.board[firstDotCoord.row][firstDotCoord.col].color
        val targetDot = state.board.getOrNull(row)?.getOrNull(col) ?: return

        if (targetDot.color != chainColor) return // Must be same color

        // Backtrack / Undo if moving to second-to-last dot
        if (currentChain.size >= 2 && currentChain[currentChain.size - 2] == DotCoordinate(row, col)) {
            val poppedChain = currentChain.dropLast(1)
            val loopStillFormed = checkIfLoop(poppedChain)
            _uiState.update {
                it.copy(
                    currentChain = poppedChain,
                    isLoopFormed = loopStillFormed
                )
            }
            soundManager.playDotConnect(poppedChain.size - 1)
            soundManager.vibrateDotConnect()
            return
        }

        val targetCoord = DotCoordinate(row, col)
        // Check if forming a closed loop (touching a dot already in the chain that is not immediate prev)
        if (currentChain.contains(targetCoord)) {
            if (currentChain.size >= 4 && !state.isLoopFormed) {
                // Loop closed!
                _uiState.update {
                    it.copy(
                        currentChain = currentChain + targetCoord,
                        isLoopFormed = true
                    )
                }
                soundManager.playLoopCelebration()
                soundManager.vibrateLoopMade()
            }
            return
        }

        // Normal addition to chain (if not already locked in a closed loop)
        if (!state.isLoopFormed) {
            val newChain = currentChain + targetCoord
            _uiState.update {
                it.copy(currentChain = newChain)
            }
            soundManager.playDotConnect(newChain.size - 1)
            soundManager.vibrateDotConnect()
        }
    }

    private fun checkIfLoop(chain: List<DotCoordinate>): Boolean {
        if (chain.size < 4) return false
        val set = mutableSetOf<DotCoordinate>()
        for (coord in chain) {
            if (!set.add(coord)) return true
        }
        return false
    }

    fun onDotTouchEnd(centerX: Float = 0f, centerY: Float = 0f) {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver) return
        val chain = state.currentChain

        if (chain.size < 2) {
            _uiState.update { it.copy(currentChain = emptyList(), isLoopFormed = false) }
            return
        }

        val chainColor = state.board[chain.first().row][chain.first().col].color

        if (state.isLoopFormed) {
            // SQUARES / LOOP CLEARED: Eliminate ALL dots of this color on the entire board!
            eliminateAllOfColor(chainColor)
        } else {
            // CHAIN CLEARED: Eliminate only dots in the chain
            eliminateChain(chain)
        }
    }

    private fun eliminateChain(chain: List<DotCoordinate>) {
        val state = _uiState.value
        val chainSet = chain.toSet()
        val dotsClearedCount = chainSet.size
        val points = dotsClearedCount * 10 + (if (dotsClearedCount >= 5) (dotsClearedCount - 4) * 20 else 0)

        soundManager.playPop()
        soundManager.vibrateDotConnect()

        val newBoard = state.board.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, dot ->
                if (chainSet.contains(DotCoordinate(r, c))) {
                    dot.copy(isPopping = true)
                } else dot
            }
        }

        val newMaxChain = maxOf(state.maxChainLength, dotsClearedCount)
        val newScore = state.score + points
        val newDotsCleared = state.dotsCleared + dotsClearedCount
        val newMovesLeft = if (state.gameMode == GameMode.MOVES) state.movesLeft - 1 else state.movesLeft

        _uiState.update {
            it.copy(
                board = newBoard,
                currentChain = emptyList(),
                isLoopFormed = false,
                score = newScore,
                dotsCleared = newDotsCleared,
                maxChainLength = newMaxChain,
                movesLeft = newMovesLeft
            )
        }

        viewModelScope.launch {
            delay(150)
            collapseAndRefillBoard()
            checkMovesEndCondition()
        }
    }

    private fun eliminateAllOfColor(color: DotColor) {
        val state = _uiState.value
        var matchCount = 0

        val newBoard = state.board.map { rowList ->
            rowList.map { dot ->
                if (dot.color == color) {
                    matchCount++
                    dot.copy(isPopping = true)
                } else dot
            }
        }

        val bonus = 100
        val points = (matchCount * 25) + bonus
        soundManager.playPop()
        soundManager.playLoopCelebration()
        soundManager.vibrateLoopMade()

        val newScore = state.score + points
        val newDotsCleared = state.dotsCleared + matchCount
        val newSquares = state.squaresFormed + 1
        val newMovesLeft = if (state.gameMode == GameMode.MOVES) state.movesLeft - 1 else state.movesLeft

        _uiState.update {
            it.copy(
                board = newBoard,
                currentChain = emptyList(),
                isLoopFormed = false,
                score = newScore,
                dotsCleared = newDotsCleared,
                squaresFormed = newSquares,
                movesLeft = newMovesLeft
            )
        }

        viewModelScope.launch {
            delay(200)
            collapseAndRefillBoard()
            checkMovesEndCondition()
        }
    }

    private fun collapseAndRefillBoard() {
        val state = _uiState.value
        val oldBoard = state.board

        // For each column, filter out popped dots, drop remaining down, and fill top with new random dots
        val newBoardCols = mutableListOf<List<DotItem>>()

        for (c in 0 until cols) {
            val columnSurvivors = mutableListOf<DotItem>()
            for (r in 0 until rows) {
                val dot = oldBoard[r][c]
                if (!dot.isPopping) {
                    columnSurvivors.add(dot)
                }
            }

            val needed = rows - columnSurvivors.size
            val newDots = List(needed) {
                DotItem(
                    id = UUID.randomUUID().toString(),
                    row = 0,
                    col = c,
                    color = DotColor.random()
                )
            }

            val fullColumn = newDots + columnSurvivors
            newBoardCols.add(fullColumn)
        }

        // Reconstruct rows from columns
        val updatedBoard = List(rows) { r ->
            List(cols) { c ->
                newBoardCols[c][r].copy(row = r, col = c, isPopping = false, isLoopTarget = false)
            }
        }

        _uiState.update { it.copy(board = updatedBoard) }
    }

    private fun checkMovesEndCondition() {
        val state = _uiState.value
        if (state.gameMode == GameMode.MOVES && state.movesLeft <= 0) {
            endGame()
        }
    }

    // Power-ups
    fun activatePowerUp(powerUp: PowerUpType) {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver) return

        when (powerUp) {
            PowerUpType.ERASER -> {
                if (state.erasersLeft <= 0) return
                // Toggle mode
                val nextActive = if (state.activePowerUp == PowerUpType.ERASER) null else PowerUpType.ERASER
                _uiState.update { it.copy(activePowerUp = nextActive) }
            }
            PowerUpType.COLOR_BOMB -> {
                if (state.colorBombsLeft <= 0) return
                useColorBomb()
            }
            PowerUpType.SHUFFLE -> {
                if (state.shufflesLeft <= 0) return
                useShuffle()
            }
        }
    }

    private fun applyEraser(row: Int, col: Int) {
        val state = _uiState.value
        soundManager.playPop()
        soundManager.vibrateDotConnect()

        val newBoard = state.board.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, dot ->
                if (r == row && c == col) dot.copy(isPopping = true) else dot
            }
        }

        _uiState.update {
            it.copy(
                board = newBoard,
                activePowerUp = null,
                erasersLeft = it.erasersLeft - 1,
                score = it.score + 5,
                dotsCleared = it.dotsCleared + 1
            )
        }

        viewModelScope.launch {
            delay(150)
            collapseAndRefillBoard()
        }
    }

    private fun useColorBomb() {
        val state = _uiState.value
        // Find the color with highest count on board
        val colorCounts = mutableMapOf<DotColor, Int>()
        state.board.flatten().forEach { dot ->
            colorCounts[dot.color] = (colorCounts[dot.color] ?: 0) + 1
        }
        val topColor = colorCounts.maxByOrNull { it.value }?.key ?: DotColor.CORAL

        soundManager.playBlast()
        _uiState.update {
            it.copy(
                colorBombsLeft = it.colorBombsLeft - 1,
                activePowerUp = null
            )
        }

        eliminateAllOfColor(topColor)
    }

    private fun useShuffle() {
        val state = _uiState.value
        val allColors = state.board.flatten().map { it.color }.shuffled(Random(System.currentTimeMillis()))
        var index = 0

        val shuffledBoard = state.board.map { rowList ->
            rowList.map { dot ->
                dot.copy(color = allColors[index++])
            }
        }

        soundManager.playBlast()
        soundManager.vibrateLoopMade()
        _uiState.update {
            it.copy(
                board = shuffledBoard,
                shufflesLeft = it.shufflesLeft - 1,
                activePowerUp = null
            )
        }
    }

    private fun endGame() {
        timerJob?.cancel()
        val state = _uiState.value
        if (state.isGameOver) return

        val duration = when (state.gameMode) {
            GameMode.TIMED -> 60 - state.timeLeftSec
            else -> 60
        }

        val record = GameRecord(
            gameMode = state.gameMode.name,
            score = state.score,
            dotsCleared = state.dotsCleared,
            squaresFormed = state.squaresFormed,
            maxChain = state.maxChainLength,
            durationSeconds = duration
        )

        viewModelScope.launch {
            repository.saveRecord(record)
        }

        _uiState.update {
            it.copy(
                isPlaying = false,
                isGameOver = true,
                bestScore = maxOf(it.bestScore, it.score)
            )
        }
    }

    private fun generateInitialBoard(): List<List<DotItem>> {
        return List(rows) { r ->
            List(cols) { c ->
                DotItem(
                    id = UUID.randomUUID().toString(),
                    row = r,
                    col = c,
                    color = DotColor.random()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
