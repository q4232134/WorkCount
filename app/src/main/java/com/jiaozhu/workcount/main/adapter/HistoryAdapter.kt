package com.jiaozhu.workcount.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jiaozhu.workcount.data.History
import com.jiaozhu.workcount.data.Preferences
import kotlinx.android.synthetic.main.item_content.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by 教主 on 2017/12/15.
 */
public class HistoryAdapter(callback: DiffCallback<History> = DiffCallback()) :
    PagedListAdapter<History, ViewHolder>(callback) {
    var onItemClickListener: OnItemClickListener<History>? = null
    var onItemLongClickListener: OnItemLongClickListener<History>? = null
    private val format = SimpleDateFormat("HH:mm MM-DD", Locale.CHINA)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            com.jiaozhu.workcount.R.layout.item_content, parent, false
        )
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position) ?: return
        with(holder.itemView) {
            mTitle.text = Preferences.getString(model.ssid)?.let { "$it(${model.ssid})" } ?: model.ssid
            mTime.text = model.createTime.let { format.format(it) }
            mLayout.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClick(model, position) ?: false
            }
        }
    }

}

class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

/**
 * 单击监听
 */
interface OnItemClickListener<T> {
    fun onItemClick(model: T, position: Int)
}

/**
 * 长按监听
 */
interface OnItemLongClickListener<T> {
    fun onItemLongClick(model: T, position: Int): Boolean
}

class DiffCallback<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }


}