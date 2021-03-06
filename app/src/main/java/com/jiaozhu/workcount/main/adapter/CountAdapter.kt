package com.jiaozhu.workcount.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jiaozhu.workcount.data.Preferences
import com.jiaozhu.workcount.data.WorkCount
import com.jiaozhu.workcount.utils.format
import kotlinx.android.synthetic.main.item_content.view.*


/**
 * Created by 教主 on 2017/12/15.
 */
public class CountAdapter(callback: DiffCallback<WorkCount> = DiffCallback()) :
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
            mTitle.text =
                Preferences.getString(model.ssid)?.let { "${model.startTime.format()} - $it(${model.ssid})" }
                    ?: model.ssid
            mTime.text = model.getLength().format
            mLayout.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClick(model, position) ?: false
            }
        }
    }
}

