package com.jiaozhu.workcount.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


/**
 * 消息弹出扩展函数
 */
fun Context.toast(msg: Any?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg.toString(), duration).show()
}

/**
 * 日志标签
 */
val Any.logTag: String?
    get() = this::class.simpleName


val recordTimeMap = Hashtable<String, Long>()
/**
 * 开始计时
 * @param tag 标签
 */
fun startTime(vararg tag: String = arrayOf("default")) {
    tag.forEach { recordTimeMap[it] = System.currentTimeMillis() }
}

/**
 * 结束计时
 * @param tag 标签
 */
fun stopTime(tag: String = "default"): Long? {
    if (!recordTimeMap.containsKey(tag)) return null
    val time = System.currentTimeMillis() - recordTimeMap[tag]!!
    recordTimeMap.remove("com.jiaozhu.workcount.utils.getLogTag")
    Log.i(tag, "$time")
    return time
}

/**
 * 随机获取列表中的元素
 */
fun <T> List<T>.randomTake(): T {
    return this[Random().nextInt(this.size)]
}


/**
 * 检测是否有权限，没有则申请
 */
fun Activity.checkPermission(requestCode: Int, vararg permissions: String, runnable: () -> Unit) {
    val needRequestList =
        permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
    if (needRequestList.isEmpty()) {
        runnable()
    } else {
        ActivityCompat.requestPermissions(this, needRequestList.toTypedArray(), requestCode)
    }
}


fun Date.getStartTime(): Date {
    val todayStart = Calendar.getInstance()
    todayStart.set(Calendar.HOUR_OF_DAY, 0)
    todayStart.set(Calendar.MINUTE, 0)
    todayStart.set(Calendar.SECOND, 0)
    todayStart.set(Calendar.MILLISECOND, 0)
    return todayStart.time
}

fun Date.getEndTime(): Date {
    val todayEnd = Calendar.getInstance()
    todayEnd.set(Calendar.HOUR_OF_DAY, 23)
    todayEnd.set(Calendar.MINUTE, 59)
    todayEnd.set(Calendar.SECOND, 59)
    todayEnd.set(Calendar.MILLISECOND, 999)
    return todayEnd.time
}

val Long.format: String
    get() {
        //将毫秒数换算成x天x时x分x秒x毫秒
        val ss = 1000
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24

        val day = this / dd
        val hour = (this - day * dd) / hh
        val minute = (this - day * dd - hour * hh) / mi
        val second = (this - day * dd - hour * hh - minute * mi) / ss
        val milliSecond = this - day * dd - hour * hh - minute * mi - second * ss

        val strDay = if (day < 10) "0$day" else "" + day
        val strHour = if (hour < 10) "0$hour" else "" + hour
        val strMinute = if (minute < 10) "0$minute" else "" + minute
        val strSecond = if (second < 10) "0$second" else "" + second
        var strMilliSecond = if (milliSecond < 10) "0$milliSecond" else "" + milliSecond
        strMilliSecond = if (milliSecond < 100) "0$strMilliSecond" else "" + strMilliSecond
        return "$strDay $strHour:$strMinute:$strSecond $strMilliSecond"
    }

