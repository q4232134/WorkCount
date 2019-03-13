package com.jiaozhu.workcount.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jiaozhu.workcount.data.WorkCount
import com.jiaozhu.workcount.utils.format
import kotlinx.android.synthetic.main.item_content.view.*
import java.util.*


/**
 * Created by 教主 on 2017/12/15.
 */
public class CountHisAdapter(callback: DiffCallback<WorkCount> = DiffCallback()) :
    ListAdapter<WorkCount, ViewHolder>(callback) {
    var onItemClickListener: OnItemClickListener<WorkCount>? = null
    var onItemLongClickListener: OnItemLongClickListener<WorkCount>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            com.jiaozhu.workcount.R.layout.item_content, parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position) ?: return
        with(holder.itemView) {
            val days = (model.getEnd().time - model.startTime.time) / (24 * 60 * 60 * 1000)
            mTitle.text =
                "${model.startTime.format("E  MM月dd日")}     ${model.startTime.format()} -- ${(model.endTime
                    ?: Date()).format()}${if (days > 0) "+$days" else ""}"
            mTime.text = model.workLength.format
            mLayout.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClick(model, position) ?: false
            }
        }
    }
}

