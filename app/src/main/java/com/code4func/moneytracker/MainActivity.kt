package com.code4func.moneytracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.code4func.moneytracker.adapter.LogAdapter
import com.code4func.moneytracker.db.MoneyTrackerDb
import com.code4func.moneytracker.db.MoneyTrackerRepo
import com.code4func.moneytracker.helper.SharePref
import com.code4func.moneytracker.helper.get
import com.code4func.moneytracker.helper.put
import com.code4func.moneytracker.model.LogType
import com.code4func.moneytracker.model.TaskLog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val moneyTrackerRepo: MoneyTrackerRepo by lazy {
        val moneyTrackerDb = MoneyTrackerDb(applicationContext)
        MoneyTrackerRepo.create(moneyTrackerDb)
    }

    private lateinit var logAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Money Tracker"

        var linearLayoutManager = LinearLayoutManager(applicationContext)
        val decoration = DividerItemDecoration(this, linearLayoutManager.orientation)

        ContextCompat.getDrawable(this, R.drawable.bg_divider)

        logAdapter = LogAdapter(
            moneyTrackerRepo = moneyTrackerRepo,
            onDeleteItem = {
                if (it.type == LogType.ADD) {
                    val currentMoney = SharePref.create(this)?.get("MONEY_ADD", 0) as Int
                    SharePref.create(this)?.put("MONEY_ADD", currentMoney - it.money)
                } else {
                    val currentMoney = SharePref.create(this)?.get("MONEY_SUBTRACT", 0) as Int
                    SharePref.create(this)?.put("MONEY_SUBTRACT", currentMoney - it.money)
                }

                if (recyclerView.adapter?.itemCount == 0) {
                    showEmptyTask()
                } else {
                    showTrackMoney()
                }
            },
            onUpdateItem = {

            }
        )

        recyclerView.apply {
            layoutManager = linearLayoutManager
            addItemDecoration(decoration)
            adapter = logAdapter
        }

        loadTasks(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            loadTasks(false)
        }
    }

    private fun loadTasks(enableDelay: Boolean) {
        showTrackMoney()

        launch {
            if (enableDelay) {
                delay(2000L)
            }
            val listTasks = moneyTrackerRepo.selectAll()

            withContext(Dispatchers.Main) {
                if (listTasks.isEmpty()) {
                    showEmptyTask()
                } else {
                    displayTasks(listTasks)
                }
            }
        }
    }

    private fun showTrackMoney() {
        val addMoney = SharePref.create(this)?.get("MONEY_ADD", 0)
        val subtractMoney = SharePref.create(this)?.get("MONEY_SUBTRACT", 0)

        tvAddMoney.text = addMoney.toString()
        tvSubtractMoney.text = subtractMoney.toString()
    }

    private fun showEmptyTask() {
        // GONE, INVISIBLE
        loading.visibility = View.GONE
        container.visibility = View.GONE

        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Empty !"


    }

    private fun displayTasks(listTasks: MutableList<TaskLog>) {
        loading.visibility = View.GONE
        tvStatus.visibility = View.GONE

        container.visibility = View.VISIBLE
        logAdapter.setData(listTasks)
    }

    fun mockTasklogs() : MutableList<TaskLog> {
        val result = mutableListOf<TaskLog>()
        for (i in 1..10) {
            if (i % 2 == 0) {
                result.add(
                    TaskLog(
                        id = i,
                        name = "Item $i",
                        money = 200 * i,
                        type = LogType.ADD
                    )
                )
            } else {
                result.add(
                    TaskLog(
                        id = i,
                        name = "Item $i",
                        money = 200 * i,
                        type = LogType.SUBTRACT
                    )
                )
            }
        }
        return result
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.addTaskLog) {
            val intent = Intent(this, AddTaskLogActivity::class.java)
            startActivityForResult(intent, 1)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}
