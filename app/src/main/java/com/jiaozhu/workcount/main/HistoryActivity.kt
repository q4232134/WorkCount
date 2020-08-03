package com.jiaozhu.workcount.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.jiaozhu.workcount.CApplication
import com.jiaozhu.workcount.R
import com.jiaozhu.workcount.data.HistoryDao
import com.jiaozhu.workcount.data.Preferences
import com.jiaozhu.workcount.data.WorkCount
import com.jiaozhu.workcount.main.adapter.CountHisAdapter
import com.jiaozhu.workcount.main.adapter.OnItemLongClickListener
import com.jiaozhu.workcount.main.viewModel.HistoryModel
import com.jiaozhu.workcount.utils.format
import com.jiaozhu.workcount.utils.getEndTime
import com.jiaozhu.workcount.utils.getStartTime
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.content_history.*
import java.util.*

class HistoryActivity : AppCompatActivity(), OnItemLongClickListener<WorkCount> {
    //长按item调整结束时间
    override fun onItemLongClick(model: WorkCount, position: Int): Boolean {
        model.endTime?.let {
            MaterialDialog(this).show {
                dateTimePicker(currentDateTime = Calendar.getInstance().apply {
                    timeInMillis = it.time
                }) { _, date ->
                    val temp = historyDao.getNodeByTime(it) ?: return@dateTimePicker
                    temp.createTime = date.time
                    historyDao.replace(temp)
                }
            }
        }
        return true
    }


    lateinit var historyDao: HistoryDao
    lateinit var viewModel: HistoryModel
    lateinit var adapter: CountHisAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val ap = application as CApplication
        historyDao = ap.db.historyDao()
        mHisRecyclerView.layoutManager = LinearLayoutManager(this)
        initViewModel()
    }

    private fun initViewModel() {
        mHisRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CountHisAdapter().apply { onItemLongClickListener = this@HistoryActivity }
        mHisRecyclerView.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(HistoryModel::class.java)
        viewModel.list.observe(this, androidx.lifecycle.Observer {
            adapter.submitList(it)
            mHisText.text =
                "${Preferences.targetDes}:   ${(it.sumByDouble { it.workLength.toDouble() } / (60 * 60 * 1000)).let {
                    String.format(
                        "%.2f",
                        it
                    )
                }} 小时"
            mHisTag.text = "${String.format(
                "%.2f",
                it.sumByDouble { it.workLength.toDouble() } / (it.size * 60 * 60 * 1000).let { if (it == 0) 1 else it })}小时/天"
        })
        viewModel.currentDate.value = Date()
        viewModel.currentDate.observe(this, androidx.lifecycle.Observer {
            supportActionBar?.title = it.format("yy年MM月")
            viewModel.list.value = getHisByTime(it)
        })
    }

    private fun getHisByTime(date: Date): List<WorkCount> {
        return historyDao.getDailyCount(
            date.getStartTime(Calendar.DAY_OF_MONTH, 1),
            date.getEndTime(
                Calendar.DAY_OF_MONTH,
                Calendar.getInstance().apply { time = date }.getActualMaximum(Calendar.DATE)
            ),
            Preferences.prefs.all.filter { it.value == Preferences.targetDes }.map { it.key })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                val i = Intent(this, HistoryActivity::class.java)
                startActivity(i)
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_last -> {
                viewModel.currentDate.value = viewModel.currentDate.value?.let {
                    val cal = Calendar.getInstance()
                    cal.time = it
                    cal.add(Calendar.MONTH, -1)
                    cal.time
                }
                return true
            }
            R.id.action_next -> {
                viewModel.currentDate.value = viewModel.currentDate.value?.let {
                    val cal = Calendar.getInstance()
                    cal.time = it
                    cal.add(Calendar.MONTH, 1)
                    cal.time
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
