package com.example.todo.viewModel

import androidx.lifecycle.*

import com.example.todo.db.model.ItemTasks

import com.example.todo.repository.TasksRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext


class TodoViewModel(
    private val tasksRepo: TasksRepo
) : ViewModel() {

    var allTasks = tasksRepo.getTasks()

    var importantTasks = tasksRepo.getImportantTasks()

    var finishedTasks = tasksRepo.getFinishedTasks()

    suspend fun upsertTask(task: ItemTasks) = tasksRepo.upsertTask(task)


    suspend fun deleteTask(task: ItemTasks) = tasksRepo.deleteTask(task)






}