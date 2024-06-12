package com.codility.todoapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codility.todoapp.R
import com.codility.todoapp.databinding.ListItemBinding
import com.codility.todoapp.helper.Todo

class MyAdapter(private var todoList: ArrayList<Todo>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    private var listener: OnClickListener? = null

    fun setListener(clickListener: OnClickListener) {
        this.listener = clickListener
    }

    fun updateData(newTodoList: List<Todo>) {
        todoList.clear()
        todoList.addAll(newTodoList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo: Todo = todoList[position]
        holder.bind(todo, listener, position)
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    inner class ViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(todo: Todo, listener: OnClickListener?, position: Int) {
            binding.apply {
                tvTitle.text = todo.title
                tvDesc.text = todo.description
                tvTimestamp.text = todo.timestamp
                tvPriority.text = "Priority: ${todo.priorityLevel}"


                when (todo.priorityLevel) {
                    "Level 1" -> tvPriority.setTextColor(ContextCompat.getColor(itemView.context, R.color.one))
                    "Level 2" -> tvPriority.setTextColor(ContextCompat.getColor(itemView.context, R.color.two))
                    "Level 3" -> tvPriority.setTextColor(ContextCompat.getColor(itemView.context, R.color.three))
                    else -> tvPriority.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }

                root.setOnClickListener {
                    listener?.onItemClick(todo, position)
                }

                btnDelete.setOnClickListener {
                    listener?.onItemDelete(todo)
                }
            }
        }
    }





    interface OnClickListener {
        fun onItemClick(todo: Todo, position: Int)
        fun onItemDelete(todo: Todo)
    }
}
