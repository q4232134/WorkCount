package com.jiaozhu.workcount.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jiaozhu.workcount.data.History

class ScrollingModel : ViewModel() {
    lateinit var lastNodeName: LiveData<String>
    lateinit var lastNode: LiveData<History>
    lateinit var historyList: LiveData<List<History>>

}
