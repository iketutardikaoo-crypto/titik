package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_tasks")
data class AssistantTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "Umum", // "Umum", "Kerja", "Belajar", "Penting"
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
