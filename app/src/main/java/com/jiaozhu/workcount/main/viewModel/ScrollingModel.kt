package com.jiaozhu.workcount.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jiaozhu.workcount.data.History
import com.jiaozhu.workcount.data.WorkCount

class ScrollingModel : ViewModel() {
    lateinit var lastNodeName: LiveData<String>
    lateinit var lastNode: LiveData<History>
    lateinit var historyList: LiveData<List<History>>
    lateinit var showList: List<WorkCount>
    var firstTargetNode: History? = null//第一个目标点

}
