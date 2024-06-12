// ScrollingActivity.kt
package com.codility.recyclerview

import com.codility.todoapp.adapter.MyAdapter
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.codility.todoapp.R
import com.codility.todoapp.databinding.ActivityScrollingBinding
import com.codility.todoapp.helper.Todo
import com.codility.todoapp.helper.TodoDao
import com.codility.todoapp.helper.TodoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScrollingActivity : AppCompatActivity(), MyAdapter.OnClickListener {

    private lateinit var binding: ActivityScrollingBinding
    private lateinit var todoDao: TodoDao
    private lateinit var myAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val db = TodoDatabase.getInstance(this)
        todoDao = db.todoDao()

        setupRecyclerView()

        binding.fab.setOnClickListener {
            showNoteDialog(false, null, -1)
        }

        getTodoList()
    }

    private fun setupRecyclerView() {
        binding.list.layoutManager = LinearLayoutManager(this)
        myAdapter = MyAdapter(ArrayList())
        myAdapter.setListener(this)
        binding.list.adapter = myAdapter
    }

    private fun getTodoList() {
        CoroutineScope(Dispatchers.IO).launch {
            val todoList = todoDao.getAllTodos().sortedWith(compareBy(
                { it.priorityLevel },
                { it.title }
            ))
            CoroutineScope(Dispatchers.Main).launch {
                myAdapter.updateData(todoList)
            }
        }
    }

    private fun deleteConfirmation(todo: Todo) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Confirm Delete...")
        alertDialog.setMessage("Are you sure you want to delete this?")
        alertDialog.setIcon(R.drawable.ic_delete)
        alertDialog.setPositiveButton("YES") { dialog, which ->
            CoroutineScope(Dispatchers.IO).launch {
                todoDao.delete(todo)
                getTodoList()
            }
        }

        alertDialog.setNegativeButton("NO") { dialog, which ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    private fun showNoteDialog(shouldUpdate: Boolean, todo: Todo?, position: Int) {
        val view = layoutInflater.inflate(R.layout.add_todo, null)
        val alertDialogView = AlertDialog.Builder(this).create()
        alertDialogView.setView(view)

        val edTitle = view.findViewById<EditText>(R.id.edTitle)
        val edDesc = view.findViewById<EditText>(R.id.edDesc)
        val btAddUpdate = view.findViewById<Button>(R.id.btAddUpdate)
        val btCancel = view.findViewById<Button>(R.id.btCancel)
        val spinnerPriority = view.findViewById<Spinner>(R.id.spinnerPriority)

        val priorityLevels = resources.getStringArray(R.array.priority_levels)

        if (shouldUpdate && todo != null) {
            edTitle.setText(todo.title)
            edDesc.setText(todo.description)
            val priorityIndex = priorityLevels.indexOf(todo.priorityLevel)
            spinnerPriority.setSelection(priorityIndex)
        }

        btAddUpdate.setOnClickListener {
            val title = edTitle.text.toString()
            val desc = edDesc.text.toString()
            val priority = spinnerPriority.selectedItem.toString()

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Enter Your Title!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (TextUtils.isEmpty(desc)) {
                Toast.makeText(this, "Enter Your Description!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTodo = if (shouldUpdate && todo != null) {
                todo.copy(title = title, description = desc, priorityLevel = priority)
            } else {
                Todo(title = title, description = desc, priorityLevel = priority, timestamp = "")
            }

            if (shouldUpdate && todo != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    todoDao.update(newTodo)
                    getTodoList()
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    todoDao.insert(newTodo)
                    getTodoList()
                }
            }
            alertDialogView.dismiss()
        }

        btCancel.setOnClickListener {
            alertDialogView.dismiss()
        }

        alertDialogView.setCancelable(false)
        alertDialogView.show()
    }


    override fun onItemDelete(todo: Todo) {
        deleteConfirmation(todo)
    }

    override fun onItemClick(todo: Todo, position: Int) {
        showNoteDialog(true, todo, position)
    }
}
