package com.example.todo.repository

import com.example.todo.db.TodoDatabase
import com.example.todo.db.model.ItemTasks


class TasksRepo(
    private val todoDatabase: TodoDatabase
) {

    fun getTasks() = todoDatabase.todoDao().getAllTasks()

    suspend fun deleteTask(task :ItemTasks) = todoDatabase.todoDao().deleteTask(task)

    suspend fun upsertTask(task : ItemTasks) = todoDatabase.todoDao().upsert(task)

    fun getImportantTasks() = todoDatabase.todoDao().getImportantItem()

    fun getFinishedTasks() = todoDatabase.todoDao().getFinishedItem()



}