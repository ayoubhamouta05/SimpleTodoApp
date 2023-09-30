package com.example.todo.ui.fragments

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.adapter.TasksAdapter
import com.example.todo.databinding.AddTaskBinding
import com.example.todo.databinding.DeleteOptionDialogBinding
import com.example.todo.databinding.FragmentTasksDetailsBinding
import com.example.todo.databinding.UncheckBoxOptionBinding
import com.example.todo.db.model.ItemTasks
import com.example.todo.notification.NotificationReceiver
import com.example.todo.ui.activities.MainActivity
import com.example.todo.viewModel.TodoViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TasksDetailsFragment : Fragment() {
    private lateinit var binding: FragmentTasksDetailsBinding
    private lateinit var viewModel: TodoViewModel
    val args by navArgs<TasksDetailsFragmentArgs>()
    lateinit var tasksType: String
    lateinit var tasksAdapter: TasksAdapter
    private val list = mutableListOf<ItemTasks>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.tasksType?.let {
            tasksType = it
        }
        viewModel = (activity as MainActivity).viewModel
        setupRv()
        getTasks(tasksType)
        binding.tasksType.text = tasksType

        tasksAdapter.setOnImportantImgClickListener {

            setImportantTaskAction(it)


        }
        tasksAdapter.setOnCheckClickListener {
            finishingTaskAction(it)
        }

        binding.fabAddTask.setOnClickListener {
            showAlertDialog()
        }

        deleteOrModifyOption()



        binding.searchEd.addTextChangedListener { editText->
            if (!editText.isNullOrEmpty()){
                viewModel.searchTasks(editText.toString()).observe(viewLifecycleOwner) { searchList ->
                    tasksAdapter.list = searchList
                }
            }else{
                getTasks(tasksType)
            }

        }

    }


    private fun setupRv() {
        tasksAdapter = TasksAdapter()
        binding.rvTasks.apply {
            adapter = tasksAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getTasks(tasksType: String) {
        when (tasksType) {
            "All Tasks" -> {
                viewModel.allTasks.observe(viewLifecycleOwner) { allTasks ->
                    tasksAdapter.list = allTasks
                }
            }

            "Important Tasks" -> {
                viewModel.importantTasks.observe(viewLifecycleOwner) { importantTasks ->
                    tasksAdapter.list = importantTasks
                }
            }

            "Completed Tasks" -> {
                viewModel.completedTasks.observe(viewLifecycleOwner) { completedTasks ->
                    tasksAdapter.list = completedTasks
                }
            }
        }

    }

    private fun setImportantTaskAction(itemTasks: ItemTasks) {

        itemTasks.isImportant = !itemTasks.isImportant

        viewModel.upsertTask(itemTasks)

        if (itemTasks.isImportant) {
            Toast.makeText(
                requireContext(),
                "Task Added To Important : You Must Finish This",
                Toast.LENGTH_SHORT
            ).show()
            list.add(itemTasks)
            // todo : make the scheduleNotification show to me more than one notification


        } else {
            if (list.contains(itemTasks)) {
                list.remove(itemTasks)
            }
        }



    }

    private fun scheduleNotification(task : ItemTasks) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val contentText = "You must finish that : \n ${task.taskText}"
        val id = task.id

        /**
         * simplify time
         */
        val dateFormat = SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm a", Locale.getDefault())
        val date = dateFormat.parse(task.timeTodo)

        // Set the time when the notification should be triggered
        val calendar = Calendar.getInstance()
        calendar.time = date!!

        // Create an Intent that will be triggered when the alarm fires
        val intent = Intent(requireActivity(), NotificationReceiver::class.java)
        intent.putExtra("TaskID",id)

        intent.putExtra("contentText",contentText)

        // Pass a unique request code
       // val pendingIntent = task.pendingIntent ?: PendingIntent.getBroadcast(requireContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Schedule the alarm
       // alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun finishingTaskAction(itemTasks: ItemTasks) {

        val dialogBinding = UncheckBoxOptionBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
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
                viewModel.upsertTask(itemTasks)
            }
        }

        itemTasks.isFinished = !itemTasks.isFinished
        viewModel.upsertTask(itemTasks)


        if (itemTasks.isFinished) {
            Toast.makeText(
                requireContext(),
                "Congratulation for finishing this Task",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun showAlertDialog() {

        val dialogBinding = AddTaskBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
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
            hour.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

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
                                    tasksType,
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

        getTasks(tasksType)
    }

    private fun deleteOrModifyOption() {

        tasksAdapter.setOnLongItemClickListener { itemTask ->

            val dialogBinding = DeleteOptionDialogBinding.inflate(layoutInflater)
            dialogBinding.edModifyText.setText(itemTask.taskText)
            val dialog = Dialog(requireContext())
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
                    val simpleDateFormat = SimpleDateFormat("EEEE,  dd-MMM-yyyy  hh-mm a")
                    val dateTime: String?
                    if (dialogBinding.year.value == Calendar.getInstance().get(Calendar.YEAR) &&
                        dialogBinding.day.value == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    ) {
                        simpleDateFormat.applyPattern("'today',  dd-MMM-yyyy  hh-mm a")
                        dateTime = simpleDateFormat.format(selectedCalendar.timeInMillis)
                    } else {
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

//    private fun deleteTasksWhenTimeEnd() {
//
//
//        viewModel.allTasks.observe(viewLifecycleOwner) { allTasks ->
//            val df = SimpleDateFormat("EEEE,  dd-MMM-yyyy  hh-mm a")
//
//            //todo : delete the task when his time is ended
//            for (itemTask in allTasks) {
//                val time = df.parse(itemTask.timeTodo)!!
//                val currentDay = Calendar.getInstance()
//                currentDay.set(
//                    Calendar.getInstance().get(Calendar.YEAR),
//                    Calendar.getInstance().get(Calendar.MONTH),
//                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH) -1,
//                    23,
//                    59,
//                    59
//                )
//
//                if (time.before(currentDay.time)) {
//                    viewModel.deleteTask(itemTask)
//                }
//            }
//
//        }
//    }


}