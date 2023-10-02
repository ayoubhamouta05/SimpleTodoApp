package com.example.todo.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.ActivityMainBinding

import com.example.todo.db.TodoDatabase
import com.example.todo.repository.TasksRepo

import com.example.todo.viewModel.TodoViewModel
import com.example.todo.viewModel.TodoViewModelProvider
import javax.security.auth.login.LoginException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: TodoViewModel
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tasksRepo = TasksRepo(TodoDatabase(this))
        val todoViewModelProviderFactory = TodoViewModelProvider(tasksRepo)
        viewModel = ViewModelProvider(this@MainActivity,todoViewModelProviderFactory)[TodoViewModel::class.java]

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

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
        if (intent.hasExtra("fragmentToOpen")) {
            goToImportantActivity()
        }

    }

    private fun goToImportantActivity(){

            val fragmentToOpen = intent.getStringExtra("fragmentToOpen")
            if (fragmentToOpen != null) {
                val tasksType = Bundle().apply {
                    putString("tasks_type","Important Tasks")
                }
                navController.navigate(R.id.action_mainFragment_to_tasksDetailsFragment,tasksType)
            }

    }


}