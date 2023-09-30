package com.example.todo.viewModel

import androidx.lifecycle.*
import androidx.room.Query

import com.example.todo.db.model.ItemTasks


import com.example.todo.repository.TasksRepo
import java.util.concurrent.Executors


class TodoViewModel(
    private val tasksRepo: TasksRepo
) : ViewModel() {
    private val executor = Executors.newSingleThreadExecutor()
    var allTasks = tasksRepo.getTasks()

    var importantTasks = tasksRepo.getImportantTasks()

    var completedTasks = tasksRepo.getCompletedTasks()

    fun searchTasks(searchQuery: String) = tasksRepo.searchTasks(searchQuery)

     fun upsertTask(task: ItemTasks){
         executor.execute {
             tasksRepo.upsertTask(task)
         }
     }

     fun deleteTask(task: ItemTasks) {
         executor.execute {
             tasksRepo.deleteTask(task)
         }
     }

}