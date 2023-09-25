package com.example.todo.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todo.repository.TasksRepo

class TodoViewModelProvider(
    private val tasksRepo : TasksRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodoViewModel(tasksRepo) as T
    }
}
