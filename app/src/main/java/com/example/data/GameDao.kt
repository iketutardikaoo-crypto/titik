package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GameRecord>>

    @Query("SELECT * FROM game_records WHERE gameMode = :mode ORDER BY score DESC LIMIT 10")
    fun getTopScoresForMode(mode: String): Flow<List<GameRecord>>

    @Query("SELECT MAX(score) FROM game_records WHERE gameMode = :mode")
    fun getHighScoreForMode(mode: String): Flow<Int?>

    @Query("SELECT SUM(dotsCleared) FROM game_records")
    fun getTotalDotsCleared(): Flow<Int?>

    @Query("SELECT SUM(squaresFormed) FROM game_records")
    fun getTotalSquaresFormed(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM game_records")
    fun getTotalGamesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecord): Long

    @Query("DELETE FROM game_records")
    suspend fun clearAllRecords()
}
