package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameMode: String, // "TIMED", "MOVES", "ZEN", "DOTS_AND_BOXES"
    val score: Int,
    val dotsCleared: Int = 0,
    val squaresFormed: Int = 0,
    val maxChain: Int = 0,
    val durationSeconds: Int = 0,
    val isWin: Boolean = false, // Used for Dots and Boxes
    val timestamp: Long = System.currentTimeMillis()
)
