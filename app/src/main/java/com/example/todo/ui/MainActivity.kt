package com.example.todo.ui

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.adapter.ItemTypeAdapter
import com.example.todo.databinding.ActivityMainBinding
import com.example.todo.db.TodoDatabase
import com.example.todo.db.model.ItemType
import com.example.todo.repository.TasksRepo
import com.example.todo.viewModel.TodoViewModel
import com.example.todo.viewModel.TodoViewModelProvider
import kotlinx.coroutines.*
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var itemAdapter: ItemTypeAdapter

    private lateinit var viewModel: TodoViewModel

    private var numberAllItemTasks: Int? = null

    private var numberImportant: Int? = null

    private var numberFinished : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActivity()

    }

    private fun setupActivity() {
        val tasksRepo = TasksRepo(TodoDatabase(this))
        val todoViewModelProviderFactory = TodoViewModelProvider(tasksRepo)

        intent.extras?.getInt("task_number")
        intent.extras?.getString("task_type")

        lifecycleScope.launch {
            viewModel = ViewModelProvider(
                this@MainActivity,
                todoViewModelProviderFactory
            )[TodoViewModel::class.java]

            setupRecyclerView()
        }

    }




    private fun setupRecyclerView() {


        lifecycleScope.launch {


            itemAdapter = ItemTypeAdapter(this@MainActivity)


            viewModel.allTasks.observe(this@MainActivity) { allItemTasks ->
                numberAllItemTasks = allItemTasks.size
                binding.tvAllTasks.text = "Today You Have ${allItemTasks.size} Tasks"
                // apdated from here
                viewModel.importantTasks.observe(this@MainActivity){ importantList->
                    numberImportant =  importantList.size
                    // complete the finish list
                    viewModel.finishedTasks.observe(this@MainActivity){finishedList->
                        numberFinished = finishedList.size

                        val list = listOf(
                            ItemType(
                                "All Tasks",
                                numberAllItemTasks,
                                R.drawable.calendar
                            ),
                            ItemType(
                                "Important",
                                numberImportant,
                                R.drawable.add_important_ic
                            ),
                            ItemType(
                                "Finished",
                                numberFinished,
                                R.drawable.done_all_ic
                            )
                        )
                        binding.rvItemType.apply {
                            itemAdapter.list = list
                            layoutManager =
                                LinearLayoutManager(this@MainActivity)
                            adapter = itemAdapter
                        }
                    }
                }

            }

        }

    }
}