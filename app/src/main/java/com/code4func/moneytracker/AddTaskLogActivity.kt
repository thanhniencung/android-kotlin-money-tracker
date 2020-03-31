package com.code4func.moneytracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.code4func.moneytracker.db.MoneyTrackerDb
import com.code4func.moneytracker.db.MoneyTrackerRepo
import com.code4func.moneytracker.helper.SharePref
import com.code4func.moneytracker.helper.get
import com.code4func.moneytracker.helper.put
import com.code4func.moneytracker.model.LogType
import com.code4func.moneytracker.model.TaskLog
import io.ghyeok.stickyswitch.widget.StickySwitch
import kotlinx.android.synthetic.main.activity_add_task_log.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AddTaskLogActivity : AppCompatActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val moneyTrackerRepo: MoneyTrackerRepo by lazy {
        val moneyTrackerDb = MoneyTrackerDb(applicationContext)
        MoneyTrackerRepo.create(moneyTrackerDb)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task_log)

        title = "Add Task Log"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var logType = LogType.ADD
        stickySwitch.setA(object: StickySwitch.OnSelectedChangeListener {
            override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                logType = LogType.valueOf(text)
            }
        })

        btnAddTask.setOnClickListener {
            val taskName = extendedEditTaskName.text.toString()
            val money = extendedEditMoney.text.toString()

            launch {
                moneyTrackerRepo.insert(
                    TaskLog(
                        name = taskName,
                        money = money.toInt(),
                        type = logType
                    )
                )

                calculateTrackingMoney(logType, money.toInt())

                finish()
                setResult(1)
            }
        }

    }

    private fun calculateTrackingMoney(logType: LogType, money: Int) {
        if (logType == LogType.ADD) {
            val currentMoney = SharePref.create(this)?.get("MONEY_ADD", 0) as Int
            SharePref.create(this)?.put("MONEY_ADD", currentMoney + money)
        } else {
            val currentMoney = SharePref.create(this)?.get("MONEY_SUBTRACT", 0) as Int
            SharePref.create(this)?.put("MONEY_SUBTRACT", currentMoney + money)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
