package com.example.todo.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.todo.db.model.ItemTasks
import com.example.todo.db.model.ItemType


@Dao
interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: ItemTasks)

    @Query("Select * From tasks_table")
    fun getAllTasks(): LiveData<List<ItemTasks>>

    @Delete
    suspend fun deleteTask(task : ItemTasks)

    @Query("Select * From tasks_table WHERE isImportant = 1")
    fun getImportantItem() : LiveData<List<ItemTasks>>

    @Query("Select * From tasks_table WHERE isFinished = 1")
    fun getFinishedItem() : LiveData<List<ItemTasks>>



}