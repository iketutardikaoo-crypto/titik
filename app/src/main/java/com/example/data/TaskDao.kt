package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM assistant_tasks ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<AssistantTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AssistantTask): Long

    @Update
    suspend fun updateTask(task: AssistantTask)

    @Delete
    suspend fun deleteTask(task: AssistantTask)

    @Query("DELETE FROM assistant_tasks WHERE isCompleted = 1")
    suspend fun clearCompletedTasks()
}
