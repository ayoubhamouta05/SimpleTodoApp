package com.example.todo.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.databinding.TaskItemBinding
import com.example.todo.db.model.ItemTasks

class TasksAdapter() : RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    class ViewHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TaskItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private val differCallBack = object : DiffUtil.ItemCallback<ItemTasks>() {
        override fun areItemsTheSame(oldItem: ItemTasks, newItem: ItemTasks): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ItemTasks, newItem: ItemTasks): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, differCallBack)

    var list: List<ItemTasks>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount(): Int = differ.currentList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = list[position]
        holder.binding.apply {
            taskTV.text = list.taskText
            checkTask.isChecked = list.isFinished
            setImportantImg.isChecked = list.isImportant

            timeOfTask.text = list.timeTodo


            setImportantImg.setOnClickListener {
                if (list.isFinished){
                   // list.isImportant = false
                    setImportantImg.isChecked = false
                    Toast.makeText(root.context,"This Task is Already Finish",Toast.LENGTH_SHORT).show()
                }else{
                    onImportantImgClickListener?.let { it(list) }
                }

            }


            checkTask.setOnClickListener { onCheckClickListener?.let {
                if (checkTask.isChecked){
                    taskTV.paintFlags = taskTV.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    taskTV.setTextColor(root.resources.getColor(R.color.transparent,null))
                    list.isImportant = false
                    setImportantImg.isChecked = false
                }else{
                    taskTV.paintFlags = taskTV.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    taskTV.setTextColor(root.resources.getColor(R.color.black,null))
                }
                it(list)

            }

            }
            if (list.isFinished){
                taskTV.paintFlags = taskTV.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                taskTV.setTextColor(root.resources.getColor(R.color.transparent,null))
                list.isImportant = false
                setImportantImg.isChecked = false
            }
            else{
                taskTV.paintFlags = taskTV.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskTV.setTextColor(root.resources.getColor(R.color.black,null))
            }

            root.apply {
                setOnLongClickListener {
                    if(!list.isFinished)
                        onLongItemClickListener?.let { it(list) }
                    false
                }
            }
        }
    }

    private var onCheckClickListener: ((ItemTasks) -> Unit)? = null

    fun setOnCheckClickListener(listener: (ItemTasks) -> Unit) {
        onCheckClickListener = listener
    }

    private var onLongItemClickListener: ((ItemTasks) -> Unit)? = null

    fun setOnLongItemClickListener(listener: (ItemTasks) -> Unit) {
        onLongItemClickListener = listener
    }

    private var onImportantImgClickListener: ((ItemTasks) -> Unit)? = null

    fun setOnImportantImgClickListener(listener: (ItemTasks) -> Unit) {
        onImportantImgClickListener = listener
    }


}