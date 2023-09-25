package com.example.todo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todo.db.model.ItemTasks

@Database(entities = [ItemTasks::class], version = 1, exportSchema = true)

abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao() :TodoDao

    companion object {
        @Volatile
        var INSTANCE: TodoDatabase? = null

        private val LOCK = Any()
        operator fun invoke (context: Context) = INSTANCE ?: synchronized(LOCK){
            INSTANCE ?: createDatabase(context).also{
                INSTANCE = it
            }
        }

        private fun createDatabase(context: Context)= Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            "tasks_table"
        ).fallbackToDestructiveMigration().build()
    }

}