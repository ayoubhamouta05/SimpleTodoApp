package com.example.todo.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.todo.db.model.ItemTasks
import kotlin.reflect.KClass


@Dao
interface TodoDao {

    @Upsert(entity = ItemTasks::class)
    fun upsert(itemTasks: ItemTasks)

    @Query("Select * From tasks_table")
    fun getAllTasks(): LiveData<List<ItemTasks>>

    @Delete(entity = ItemTasks::class)
     fun deleteTask( task : ItemTasks)

    @Query("Select * From tasks_table WHERE isImportant = 1")
    fun getImportantItem() : LiveData<List<ItemTasks>>

    @Query("Select * From tasks_table WHERE isFinished = 1")
    fun getCompletedItem() : LiveData<List<ItemTasks>>

    @Query("Select * from tasks_table WHERE taskText like :searchQuery || '%' ")
    fun searchTasks(searchQuery : String) : LiveData<List<ItemTasks>>


}