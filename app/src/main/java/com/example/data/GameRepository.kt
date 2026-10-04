package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val allRecords: Flow<List<GameRecord>> = gameDao.getAllRecords()

    fun getTopScores(mode: String): Flow<List<GameRecord>> = gameDao.getTopScoresForMode(mode)

    fun getHighScore(mode: String): Flow<Int?> = gameDao.getHighScoreForMode(mode)

    val totalDotsCleared: Flow<Int?> = gameDao.getTotalDotsCleared()

    val totalSquaresFormed: Flow<Int?> = gameDao.getTotalSquaresFormed()

    val totalGamesCount: Flow<Int> = gameDao.getTotalGamesCount()

    suspend fun saveRecord(record: GameRecord): Long = gameDao.insertRecord(record)

    suspend fun clearHistory() = gameDao.clearAllRecords()
}
