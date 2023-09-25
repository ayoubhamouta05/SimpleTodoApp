package com.example.todo.db.model


data class ItemType(
    val type : String ,
    var numberTasks : Int ?=null,
    val image : Int?=null,
)
