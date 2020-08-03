package com.jiaozhu.workcount.main

import android.Manifest
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
import com.jiaozhu.workcount.data.HistoryDao
import com.jiaozhu.workcount.data.Preferences
import com.jiaozhu.workcount.data.WorkCount
import com.jiaozhu.workcount.data.ssidDes
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
import ru.alexbykov.nopermission.PermissionHelper
import java.text.SimpleDateFormat
import java.util.*


class ScrollingActivity : AppCompatActivity(), OnItemLongClickListener<WorkCount> {
    lateinit var historyDao: HistoryDao
    lateinit var viewModel: ScrollingModel
    lateinit var adapter: CountAdapter
    lateinit var dialog: MaterialDialog
    lateinit var permissionHelper: PermissionHelper
    private val apFormat = SimpleDateFormat("HH", Locale.CHINA)
    //午休时间
    val sleepTime
        get() = viewModel.firstTargetNode?.let {
            if (apFormat.format(it.createTime).toInt() <= 12 &&
                apFormat.format(Date()).toInt() >= 14
            ) 2 * 60 * 60 * 1000 else 0
        } ?: 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.jiaozhu.workcount.R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            genCountList(viewModel.showList)
            dialog.show()
        }
//        fab.setOnLongClickListener {
//            getDatabasePath("database").let {
//                Tools.copyFile(
//                    it.path,
//                    getExternalFilesDir(null)
//                        .path + File.separator + "back.db"
//                )
//            }
//            toast("导出完成")
//            true
//        }
        val ap = application as CApplication
        ap.serviceStartTime = Date()
        historyDao = ap.db.historyDao()

        initViewModel()
        val i = Intent(this, WorkService::class.java)
        startService(i)
        permissionHelper = PermissionHelper(this)
        getWifiSSidPermission()
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
            toolbar_layout.title = it?.ssidDes
        })
        viewModel.historyList.observe(this, Observer {
            viewModel.firstTargetNode = it.firstOrNull { it.ssid.ssidDes == Preferences.targetDes }
            val zip = it.zipWithNext { a, b -> WorkCount(a.ssid, a.createTime, b.createTime) }.reversed()
            viewModel.showList = zip
            adapter.submitList(zip)
        })
        viewModel.lastNode.observe(
            this,
            Observer {
                //如果当前地点为公司，则显示公司总工作时长，否则显示当前地点时长
                val startTime =
                    if (viewModel.lastNodeName.value?.ssidDes == Preferences.targetDes) (viewModel.firstTargetNode?.createTime
                        ?: Date()).time + sleepTime else it?.createTime?.time ?: Date().time
                mMeter.base = SystemClock.elapsedRealtime() - (Date().time - startTime)
                mMeter.start()
            })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.jiaozhu.workcount.R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.jiaozhu.workcount.R.id.action_history -> {
                val i = Intent(this, HistoryActivity::class.java)
                startActivity(i)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemLongClick(model: WorkCount, position: Int): Boolean {
        showTextDialog(model)
        return true
    }

    private fun showTextDialog(model: WorkCount) {
        MaterialDialog(this).title(text = "标记").message(text = "设置${model.ssid}的地点：")
            .neutralButton(text = "设置为目标") { Preferences.targetDes = model.ssid.ssidDes }.show {
                input(hint = model.ssid.ssidDes) { _, text ->
                    Preferences.setString(model.ssid, text.toString())
                    toast("设置成功")
                    adapter.notifyDataSetChanged()
                }.show()
            }
    }

    private fun genCountList(list: List<WorkCount>) {
        val counts = list.groupBy { it.ssid.ssidDes }.values.map {
            WorkCount(
                it.first().ssid,
                it.first().startTime,
                it.last().endTime,
                it.map { it.getLength() }.sum()
            )
        }.sortedByDescending(WorkCount::getLength)
        val startTime = list.lastOrNull { it.des.ssidDes == Preferences.targetDes }?.startTime
        var endTime = list.firstOrNull { it.des.ssidDes == Preferences.targetDes }?.endTime
        if (viewModel.lastNodeName.value?.ssidDes == Preferences.targetDes) endTime = Date()
        val desList = if (startTime != null && endTime != null)
            mutableListOf<String>("${startTime.format()}<---->${endTime.format()}    ${(endTime.time - startTime.time - sleepTime).format}") else mutableListOf()
        desList.addAll(counts.map { "${it.ssid.ssidDes}     ${it.getLength().format}" })
        dialog = MaterialDialog(this).show {
            listItems(items = desList)
            { dialog, index, text -> }
        }
    }


    private fun getWifiSSidPermission() {
        permissionHelper.check(Manifest.permission.ACCESS_FINE_LOCATION)
            .onSuccess { }.onDenied { getWifiSSidPermission() }
            .onNeverAskAgain {
                toast("权限被拒绝，9.0系统无法获取SSID")
                finish()
            }.run()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

