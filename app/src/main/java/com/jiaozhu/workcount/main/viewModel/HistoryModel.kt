package com.jiaozhu.workcount.main.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jiaozhu.workcount.data.WorkCount
import java.util.*

class HistoryModel : ViewModel() {
    var list: MutableLiveData<List<WorkCount>> = MutableLiveData()
    var currentDate: MutableLiveData<Date> = MutableLiveData()

}
