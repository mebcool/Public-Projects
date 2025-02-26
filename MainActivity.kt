package com.example.todolisthw14

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.graphics.Color

data class ToDo(val text: String, val priority: String)

class ItemAdapter(val data: MutableList<ToDo>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewItem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.todo_item_layout, parent, false)
        return ItemViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = data[position]
        holder.textView.text = "${currentItem.text} - ${currentItem.priority}"

        when (currentItem.priority) {
            "Low" -> holder.itemView.setBackgroundColor(Color.GREEN)
            "Medium" -> holder.itemView.setBackgroundColor(Color.YELLOW)
            "High" -> holder.itemView.setBackgroundColor(Color.RED)
        }
        holder.itemView.findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            data.removeAt(position)
            notifyDataSetChanged()
        }
    }
    override fun getItemCount(): Int {
        return data.size
    }
    fun addToDoItem(todo: ToDo) {
        data.add(todo)
        notifyDataSetChanged()
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var editTextItem: EditText
    private lateinit var buttonAdd: Button
    private lateinit var radioGroupPriority: RadioGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var toDoAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextItem = findViewById(R.id.editTextItem)
        buttonAdd = findViewById(R.id.buttonAdd)
        radioGroupPriority = findViewById(R.id.radioGroupPriority)
        recyclerView = findViewById(R.id.recyclerView)

        toDoAdapter = ItemAdapter(mutableListOf())
        recyclerView.adapter = toDoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        buttonAdd.setOnClickListener {
            val todoText = editTextItem.text.toString()
            val priority = when (radioGroupPriority.checkedRadioButtonId) {
                R.id.radioButtonLow -> "Low"
                R.id.radioButtonMedium -> "Medium"
                R.id.radioButtonHigh -> "High"
                else -> "Low"
            }
            val todo = ToDo(todoText, priority)
            toDoAdapter.addToDoItem(todo)
            editTextItem.text.clear()
            radioGroupPriority.clearCheck()
        }
    }
}
