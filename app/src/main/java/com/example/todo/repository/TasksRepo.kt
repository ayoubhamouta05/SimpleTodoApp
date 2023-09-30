package com.example.todo.repository

import com.example.todo.db.TodoDatabase
import com.example.todo.db.model.ItemTasks


class TasksRepo(
    private val todoDatabase: TodoDatabase
) {

    fun getTasks() = todoDatabase.todoDao().getAllTasks()

    fun deleteTask(task :ItemTasks) = todoDatabase.todoDao().deleteTask(task)

    fun upsertTask(task : ItemTasks) = todoDatabase.todoDao().upsert(task)

    fun getImportantTasks() = todoDatabase.todoDao().getImportantItem()

    fun getCompletedTasks() = todoDatabase.todoDao().getCompletedItem()

    fun searchTasks(searchQuery : String ) = todoDatabase.todoDao().searchTasks(searchQuery)



}