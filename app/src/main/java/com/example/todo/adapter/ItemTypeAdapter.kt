package com.example.todo.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AbsListView.RecyclerListener
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.ActivityMainBinding
import com.example.todo.databinding.TodoTypeItemBinding
import com.example.todo.db.model.ItemType
import com.example.todo.ui.MainActivity
import com.example.todo.ui.TasksDetails
import kotlin.coroutines.coroutineContext

class ItemTypeAdapter(var context : Context): RecyclerView.Adapter<ItemTypeAdapter.ViewHolder>() {
    inner class ViewHolder( val binding: TodoTypeItemBinding ) :RecyclerView.ViewHolder(binding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<ItemType>() {
        override fun areItemsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
            return oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: ItemType, newItem: ItemType): Boolean {
            return oldItem.numberTasks == newItem.numberTasks
        }

    }

    private var differ = AsyncListDiffer(this,differCallBack)
    var list : List<ItemType>
    get() = differ.currentList
    set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TodoTypeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int =list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {



        val list = list[position]
        holder.binding.apply {
            tvTypeName.text = list.type
            tvNumberTasks.text = "You Have ${list.numberTasks} tasks"
            userImg.setImageResource(list.image!!)

            root.setOnClickListener {
                val intent = Intent(context,TasksDetails::class.java)
                intent.putExtra("task_type", list.type)
                intent.putExtra("task_number",list.numberTasks)
                intent.putExtra("task_image",list.image)
                context.startActivity(intent)
            }
        }
    }
}