package com.example.todo.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R

import com.example.todo.adapter.TasksAdapter
import com.example.todo.databinding.ActivityTasksDetailsBinding
import com.example.todo.databinding.AddTaskBinding
import com.example.todo.databinding.DeleteOptionDialogBinding
import com.example.todo.databinding.UncheckBoxOptionBinding
import com.example.todo.db.TodoDatabase
import com.example.todo.db.model.ItemTasks
import com.example.todo.repository.TasksRepo
import com.example.todo.viewModel.TodoViewModel
import com.example.todo.viewModel.TodoViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.days

class TasksDetails() : AppCompatActivity() {
    private lateinit var binding: ActivityTasksDetailsBinding
    lateinit var viewModel: TodoViewModel

    private lateinit var tasksAdapter: TasksAdapter
    private val list = mutableListOf<ItemTasks>()


    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityTasksDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActivity()

        binding.fabAddTask.setOnClickListener {
            showAlertDialog()
        }

        tasksAdapter.setOnCheckClickListener { itemTasks ->
            finishingTaskAction(itemTasks)
        }

        tasksAdapter.setOnImportantImgClickListener { itemTasks ->
            setImportantTaskAction(itemTasks)
        }

        deleteOrModifyOption()


        deleteTasksWhenTimeEnd()

    }

    private fun setupActivity() {
        val tasksRepo = TasksRepo(TodoDatabase(this))
        val todoViewModelProviderFactory = TodoViewModelProvider(tasksRepo)

        setupRecyclerView()

        val taskType = intent.extras?.getString("task_type")
        val taskImage = intent.extras?.getInt("task_image")

        binding.tvTypeTask.text = taskType
        binding.taskTypeImg.setImageResource(taskImage!!)

        lifecycleScope.launch {
            viewModel = ViewModelProvider(
                this@TasksDetails,
                todoViewModelProviderFactory
            )[TodoViewModel::class.java]
            getTasks(taskType!!)
        }
    }


    private fun setImportantTaskAction(itemTasks: ItemTasks) {

        itemTasks.isImportant = !itemTasks.isImportant

        lifecycleScope.launch {
            viewModel.upsertTask(itemTasks)
        }

        if (itemTasks.isImportant) {
            Toast.makeText(
                this@TasksDetails,
                "Task Added To Important : You Must Finish This",
                Toast.LENGTH_SHORT
            ).show()
            list.add(itemTasks)

           // todo : make the scheduleNotification show to me more than one notification
            //scheduleNotification(time,itemTasks.taskText)
        }
        else{
            if(list.contains(itemTasks)){
                list.remove(itemTasks)
            }
        }

    }

    private fun finishingTaskAction(itemTasks: ItemTasks) {

        // todo : make a special adapter for Finished Tasks

        val dialogBinding = UncheckBoxOptionBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.create()

        if (itemTasks.isFinished) {
            dialog.show()
            dialogBinding.btnUncheck.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.btnLetChecked.setOnClickListener {
                itemTasks.isFinished = true
                dialog.dismiss()
                lifecycleScope.launch {
                    viewModel.upsertTask(itemTasks)
                }
            }
        }

        itemTasks.isFinished = !itemTasks.isFinished


        lifecycleScope.launch {
            viewModel.upsertTask(itemTasks)
        }
        if (itemTasks.isFinished) {

            Toast.makeText(
                this@TasksDetails,
                "Congratulation for finishing this Task",
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter()
        binding.rvTasks.apply {
            adapter = tasksAdapter
            layoutManager = LinearLayoutManager(this@TasksDetails)

        }

    }

    private fun getTasks(taskType: String) {

        when (taskType) {
            "All Tasks" -> {
                binding.tvTypeTask.text = "All Tasks"
                lifecycleScope.launch {
                    viewModel.allTasks.observe(this@TasksDetails) { ItemTasksList ->
                        tasksAdapter.list = ItemTasksList
                        binding.tvNumberTask.text = "${ItemTasksList.size} Tasks"
                    }
                }
            }
            "Important" -> {
                lifecycleScope.launch {
                    viewModel.importantTasks.observe(this@TasksDetails) { importantTasks ->
                        tasksAdapter.list = importantTasks
                        binding.tvNumberTask.text = "${importantTasks.size} Tasks"
                    }
                }
            }
            else -> {
                lifecycleScope.launch {
                    viewModel.finishedTasks.observe(this@TasksDetails) { ItemTasksList ->
                        tasksAdapter.list = ItemTasksList
                        binding.tvNumberTask.text = "${ItemTasksList.size} Tasks"
                    }
                }
            }
        }

    }

    private fun showAlertDialog() {

        val taskType = intent.extras?.getString("task_type")
        val dialogBinding = AddTaskBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)

        dialogBinding.apply {

            /**
             * Time Setup
             */

            year.minValue = Calendar.getInstance().get(Calendar.YEAR)
            year.maxValue = year.minValue + 10

            month.maxValue = 12
            month.value = Calendar.getInstance().get(Calendar.MONTH) + 1

            day.maxValue = 31
            day.value = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            hour.maxValue = 60
            hour.value= Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            minute.maxValue = 60
            minute.value = Calendar.getInstance().get(Calendar.MINUTE)


            month.setOnValueChangedListener { _, _, numberPicker ->
                when (numberPicker) {
                    1 -> day.maxValue = 31
                    3 -> day.maxValue = 31
                    5 -> day.maxValue = 31
                    7 -> day.maxValue = 31
                    8 -> day.maxValue = 31
                    10 -> day.maxValue = 31
                    12 -> day.maxValue = 31
                    else -> {
                        if (month.value == 2) {
                            if (Calendar.getInstance().get(Calendar.YEAR) % 4 == 0)
                                day.maxValue = 29
                            else day.maxValue = 28
                        } else
                            day.maxValue = 30
                    }
                }

            }
            /**
             * Finish Time Setup
             */

            btnCancel.setOnClickListener { dialog.dismiss() }

            //  add the Task //////
            btnAdd.setOnClickListener {

                if (dialogBinding.edAddText.text.isNullOrEmpty()) {
                    Snackbar.make(binding.root, "The Task Is Empty", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(resources.getColor(R.color.snackBarError, null))
                        .show()
                } else {
                    val selectedCalendar: Calendar = Calendar.getInstance()
                    selectedCalendar.set(
                        dialogBinding.year.value,
                        dialogBinding.month.value - 1,
                        dialogBinding.day.value,
                        dialogBinding.hour.value,
                        dialogBinding.minute.value,
                    )
                    val simpleDateFormat = SimpleDateFormat("EEEE,  dd-MMM-yyyy  hh-mm a")
                    val dateTime = simpleDateFormat.format(selectedCalendar.timeInMillis)

                    val currentCalendar = Calendar.getInstance()
                    if (selectedCalendar.before(currentCalendar)) {
                        Snackbar.make(
                            binding.root,
                            "Filed to Added The Task :\nPlease Select Time after The Current Time ",
                            Snackbar.LENGTH_LONG
                        )
                            .setBackgroundTint(resources.getColor(R.color.snackBarError, null))
                            .show()
                        dialog.dismiss()
                    } else if (selectedCalendar.after(currentCalendar)) {

                        lifecycleScope.launch {
                            viewModel.upsertTask(
                                ItemTasks(
                                    0,
                                    taskType,
                                    dialogBinding.edAddText.text.toString(),
                                    false,
                                    isImportant = false,
                                    dateTime
                                )
                            )
                        }
                        dialog.dismiss()
                        Snackbar.make(
                            binding.root,
                            "The Task Is Added Successfully",
                            Snackbar.LENGTH_SHORT
                        )
                            .setBackgroundTint(resources.getColor(R.color.snackBarSuccess, null))
                            .show()
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Filed to Added The Task :\nHarry up what Your are waiting For Do this Task Now ",
                            Snackbar.LENGTH_LONG
                        )
                            .setBackgroundTint(resources.getColor(R.color.snackBarError, null))
                            .show()
                        dialog.dismiss()
                    }
                }

            }
        }

        dialog.create()
        dialog.show()

        getTasks(taskType!!)
    }

    private fun deleteOrModifyOption() {

        tasksAdapter.setOnLongItemClickListener { itemTask ->

            val dialogBinding = DeleteOptionDialogBinding.inflate(layoutInflater)
            dialogBinding.edModifyText.setText(itemTask.taskText)
            val dialog = Dialog(this)
            dialog.setContentView(dialogBinding.root)
            dialog.setCancelable(true)
            dialog.show()
            dialogBinding.btnDelete.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.deleteTask(itemTask)
                    dialog.dismiss()
                    Snackbar.make(
                        binding.root,
                        "The Task Is Deleted Successfully",
                        Snackbar.LENGTH_SHORT
                    )
                        .setBackgroundTint(resources.getColor(R.color.snackBarSuccess, null))
                        .show()
                }
            }


            /**
             * setup date
             */

            dialogBinding.apply {
                year.minValue = Calendar.getInstance().get(Calendar.YEAR)
                year.maxValue = year.minValue + 10

                month.maxValue = 12
                month.value = Calendar.getInstance().get(Calendar.MONTH) + 1

                day.maxValue = 31
                day.value = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

                hour.maxValue = 60
                hour.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                minute.maxValue =60
                minute.value = Calendar.getInstance().get(Calendar.MINUTE)

                month.setOnValueChangedListener { _, _, numberPicker ->
                    when (numberPicker) {
                        1 -> day.maxValue = 31
                        3 -> day.maxValue = 31
                        5 -> day.maxValue = 31
                        7 -> day.maxValue = 31
                        8 -> day.maxValue = 31
                        10 -> day.maxValue = 31
                        12 -> day.maxValue = 31
                        else -> {
                            if (month.value == 2) {
                                if (Calendar.getInstance().get(Calendar.YEAR) % 4 == 0)
                                    day.maxValue = 29
                                else day.maxValue = 28
                            } else
                                day.maxValue = 30
                        }
                    }

                }
            }
            /**
             * finish Time Setup
             */

            // modify the task ////////

            dialogBinding.btnModify.setOnClickListener {

                /**
                 * get time
                 */

                if (dialogBinding.edModifyText.text.isNullOrEmpty()) {
                    Snackbar.make(binding.root, "The Task Is Empty", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(resources.getColor(R.color.snackBarError, null))
                        .show()
                } else {
                    // select the date that the user set in the Date Picker
                    val selectedCalendar: Calendar = Calendar.getInstance()
                    selectedCalendar.set(
                        dialogBinding.year.value,
                        dialogBinding.month.value - 1,
                        dialogBinding.day.value,
                        dialogBinding.hour.value,
                        dialogBinding.minute.value
                    )
                    //todo : make the simpleDateFormat write today instead of the current day
                    val simpleDateFormat =SimpleDateFormat("EEEE,  dd-MMM-yyyy  hh-mm a")
                    val dateTime :String?
                    if(dialogBinding.year.value == Calendar.getInstance().get(Calendar.YEAR)&&
                        dialogBinding.day.value == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)){
                        simpleDateFormat.applyPattern("'today',  dd-MMM-yyyy  hh-mm a")
                        dateTime = simpleDateFormat.format(selectedCalendar.timeInMillis)
                    }else{
                        dateTime = simpleDateFormat.format(selectedCalendar.timeInMillis)
                    }

                    val currentCalendar = Calendar.getInstance()

                    if (selectedCalendar.before(currentCalendar)) {
                        Snackbar.make(
                            binding.root,
                            "Filed to Update The Task :\nPlease Select Time after The Current Time ",
                            Snackbar.LENGTH_LONG
                        )
                            .setBackgroundTint(resources.getColor(R.color.snackBarError, null))
                            .show()
                        dialog.dismiss()
                    } else if (selectedCalendar.after(currentCalendar)) {
                        lifecycleScope.launch {

                            viewModel.upsertTask(
                                ItemTasks(
                                    itemTask.id,
                                    itemTask.type,
                                    dialogBinding.edModifyText.text.toString(),
                                    itemTask.isFinished,
                                    itemTask.isImportant,
                                    dateTime
                                )
                            )
                            dialog.dismiss()
                            Snackbar.make(
                                binding.root,
                                "The Task Is Updated Successfully",
                                Snackbar.LENGTH_SHORT
                            )
                                .setBackgroundTint(
                                    resources.getColor(
                                        R.color.snackBarSuccess,
                                        null
                                    )
                                )
                                .show()
                        }

                    } else {
                        Snackbar.make(
                            binding.root,
                            "Filed to Update The Task :\nHarry up what Your are waiting For Do this Task Now ",
                            Snackbar.LENGTH_LONG
                        )
                            .setBackgroundTint(resources.getColor(R.color.snackBarError, null))
                            .show()
                        dialog.dismiss()
                    }

                }

            }

        }

    }

    private fun deleteTasksWhenTimeEnd() {

        lifecycleScope.launch {
            viewModel.allTasks.observe(this@TasksDetails) { allTasks ->
                val df = SimpleDateFormat("EEEE,  dd-MMM-yyyy  hh-mm a")

    //todo : delete the task when his time is ended
                for (itemTask in allTasks) {
                    val time = df.parse(itemTask.timeTodo)!!
                    val currentDay = Calendar.getInstance()
                    currentDay.set(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)-1,
                        23,
                        59,
                        59
                    )

                    if(time.before(currentDay.time)){
                        runBlocking {
                            viewModel.deleteTask(itemTask)
                        }
                    }
                }

            }
        }
    }











}


