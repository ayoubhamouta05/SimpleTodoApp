package com.example.todo.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_table")
data class ItemTasks(
    @PrimaryKey(autoGenerate = true)
    var id : Int ,
    var type: String ?,
    var taskText : String ,
    var isFinished : Boolean ,
    var isImportant : Boolean,
    var timeTodo : String,
)
