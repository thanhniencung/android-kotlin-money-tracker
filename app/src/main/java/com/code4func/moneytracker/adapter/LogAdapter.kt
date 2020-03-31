package com.code4func.moneytracker.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.code4func.moneytracker.R
import com.code4func.moneytracker.db.MoneyTrackerDb
import com.code4func.moneytracker.db.MoneyTrackerRepo
import com.code4func.moneytracker.model.LogType
import com.code4func.moneytracker.model.TaskLog
import kotlinx.android.synthetic.main.adapter_log.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


typealias OnDeleteItem = (task: TaskLog) -> Unit
typealias OnUpdateItem = (task: TaskLog) -> Unit

class LogAdapter(
        private val dataSet: MutableList<TaskLog> = mutableListOf(),
        private val moneyTrackerRepo: MoneyTrackerRepo,
        private val onDeleteItem: OnDeleteItem,
        private val onUpdateItem: OnUpdateItem
    ) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    fun setData(dataSet: MutableList<TaskLog>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.actionItem.setOnClickListener {
                showMenuAction(it.context, it)
            }
        }

        fun bind(task: TaskLog) {
            itemView.tvTaskName.text = task.name
            if (task.type == LogType.ADD) {
                itemView.tvMoney.apply {
                    setTextColor(Color.parseColor("#669900"))
                    text = "+ ${task.money}"
                }
            } else {
                itemView.tvMoney.apply {
                    setTextColor(Color.RED)
                    text = "- ${task.money}"
                }
            }
        }

        private fun showMenuAction(context: Context, anchorView: View) {
            var popupMenu = PopupMenu(context, anchorView)
            popupMenu.apply {
                inflate(R.menu.menu_task_action)
                setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.updateTaskLog -> {
                            onUpdateItem(dataSet[adapterPosition])
                            true
                        }
                        R.id.deleteTaskLog -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                moneyTrackerRepo.delete(dataSet[adapterPosition].id)

                                withContext(Dispatchers.Main) {
                                    val currentPos = adapterPosition
                                    val task = dataSet[currentPos]

                                    dataSet.removeAt(currentPos)
                                    notifyItemRemoved(currentPos)
                                    onDeleteItem(task)
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
            }.also {
                popupMenu.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
       val view = LayoutInflater.from(parent.context)
           .inflate(R.layout.adapter_log, parent, false)
       return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}