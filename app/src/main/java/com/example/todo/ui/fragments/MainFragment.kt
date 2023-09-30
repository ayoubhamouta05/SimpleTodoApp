package com.example.todo.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentMainBinding
import com.example.todo.ui.activities.MainActivity
import com.example.todo.viewModel.TodoViewModel


class MainFragment : Fragment() {

private lateinit var binding : FragmentMainBinding
private lateinit var viewModel: TodoViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        setupClickListeners()
        observeTasksNumber()

    }

    private fun setupClickListeners(){

        binding.allTasksItem.setOnClickListener {
            val tasksType = Bundle().apply {
                putString("tasks_type","All Tasks")
            }
            findNavController().navigate(R.id.action_mainFragment_to_tasksDetailsFragment,tasksType)
        }
        binding.importantTasksItem.setOnClickListener {
            val tasksType = Bundle().apply {
                putString("tasks_type","Important Tasks")
            }
            findNavController().navigate(R.id.action_mainFragment_to_tasksDetailsFragment,tasksType)
        }
        binding.completedTasksItem.setOnClickListener {
            val tasksType = Bundle().apply {
                putString("tasks_type","Completed Tasks")
            }
            findNavController().navigate(R.id.action_mainFragment_to_tasksDetailsFragment,tasksType)
        }
    }

    private fun observeTasksNumber(){
        viewModel.allTasks.observe(viewLifecycleOwner){
            binding.allTasksNumber.text = "${it.size} Tasks"
        }
        viewModel.importantTasks.observe(viewLifecycleOwner){
            binding.importantTasksNumber.text = "${it.size} Tasks"
        }
        viewModel.completedTasks.observe(viewLifecycleOwner){
            binding.completedTasksNumber.text = "${it.size} Tasks"
        }
    }

}