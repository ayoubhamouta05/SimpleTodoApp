package com.example.todo.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.todo.R
import com.example.todo.databinding.ActivityMainBinding

import com.example.todo.db.TodoDatabase
import com.example.todo.repository.TasksRepo

import com.example.todo.viewModel.TodoViewModel
import com.example.todo.viewModel.TodoViewModelProvider

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tasksRepo = TasksRepo(TodoDatabase(this))
        val todoViewModelProviderFactory = TodoViewModelProvider(tasksRepo)
        viewModel = ViewModelProvider(this@MainActivity,todoViewModelProviderFactory)[TodoViewModel::class.java]

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        viewModel.allTasks.observe(this){
            binding.numberTasksTv.text = "You Have : ${it.size} Tasks"

        }
        navController.addOnDestinationChangedListener{_,navDestination,_->
            if(navDestination.id == R.id.mainFragment){
                binding.topBar.visibility = View.VISIBLE
            }else{
                binding.topBar.visibility = View.GONE

            }
        }
    }

}