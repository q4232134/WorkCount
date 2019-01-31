package com.jiaozhu.workcount.main

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.jiaozhu.workcount.CApplication
import com.jiaozhu.workcount.data.*
import com.jiaozhu.workcount.main.adapter.CountAdapter
import com.jiaozhu.workcount.main.adapter.OnItemLongClickListener
import com.jiaozhu.workcount.main.viewModel.ScrollingModel
import com.jiaozhu.workcount.service.WorkService
import com.jiaozhu.workcount.utils.format
import com.jiaozhu.workcount.utils.getEndTime
import com.jiaozhu.workcount.utils.getStartTime
import com.jiaozhu.workcount.utils.toast
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*
import java.util.*


class ScrollingActivity : AppCompatActivity(), OnItemLongClickListener<WorkCount> {
    lateinit var historyDao: HistoryDao
    lateinit var viewModel: ScrollingModel
    lateinit var adapter: CountAdapter
    lateinit var dialog: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.jiaozhu.workcount.R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            dialog.show()
        }
        val ap = application as CApplication
        ap.serviceStartTime = Date()
        historyDao = ap.db.historyDao()

        initViewModel()
        val i = Intent(this, WorkService::class.java)
        startService(i)

    }


    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ScrollingModel::class.java)
        viewModel.lastNodeName = historyDao.getLastNodeName()
        viewModel.historyList = historyDao.getNodeByTime(Date().getStartTime(), Date().getEndTime())
        viewModel.lastNode = historyDao.getLastNodeLive()
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CountAdapter()
        adapter.onItemLongClickListener = this
        mRecyclerView.adapter = adapter
        viewModel.lastNodeName.observe(this, Observer {
            toolbar_layout.title = it.ssidDes
        })
        viewModel.historyList.observe(this, Observer {
            val zip = it.zipWithNext { a, b -> WorkCount(a.ssid, a.createTime, b.createTime) }.reversed()
            val count = zip.groupBy { it.ssid.ssidDes }.values.map {
                WorkCount(
                    it.first().ssid,
                    it.first().startTime,
                    it.last().endTime,
                    it.map { it.length }.sum()
                )
            }.sortedByDescending(WorkCount::length)
            genCountList(count)
            adapter.submitList(zip)
        })
        viewModel.lastNode.observe(
            this,
            Observer<History> {
                mMeter.base = SystemClock.elapsedRealtime() - (Date().time - it.createTime.time)
                mMeter.start()
            })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.jiaozhu.workcount.R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.jiaozhu.workcount.R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemLongClick(model: WorkCount, position: Int): Boolean {
        showTextDialog(model)
        return true
    }

    private fun showTextDialog(model: WorkCount) {
        MaterialDialog(this).title(text = "标记").message(text = "设置${model.ssid}的地点：").show {
            input(hint = model.ssid.ssidDes) { _, text ->
                Preferences.setString(model.ssid, text.toString())
                toast("设置成功")
                adapter.notifyDataSetChanged()
            }.show()
        }
    }

    private fun genCountList(list: List<WorkCount>) {
        dialog = MaterialDialog(this).show {
            listItems(items = list.map { "${it.ssid.ssidDes}     ${it.length.format}" })
            { dialog, index, text -> }
        }
    }


}
