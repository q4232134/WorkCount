package com.jiaozhu.workcount.utils

/**
 * Created by Administrator on 2015/5/28.
 */
object Log {
    val VERBOSE = 2
    val DEBUG = 3
    val INFO = 4
    val WARN = 5
    val ERROR = 6
    val ASSERT = 7
    var level = 0

    fun v(tag: String, msg: String): Int {
        return if (level <= VERBOSE) android.util.Log.v(tag, msg) else -1
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        return if (level <= VERBOSE) android.util.Log.v(tag, msg, tr) else -1
    }

    fun d(tag: String, msg: String): Int {
        return if (level <= DEBUG) android.util.Log.d(tag, msg) else -1
    }

    fun d(tag: String, msg: String, tr: Throwable): Int {
        return if (level <= DEBUG) android.util.Log.d(tag, msg, tr) else -1
    }

    fun i(tag: String, msg: String): Int {
        return if (level <= INFO) android.util.Log.i(tag, msg) else -1
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        return if (level <= INFO) android.util.Log.i(tag, msg, tr) else -1
    }

    fun w(tag: String, msg: String): Int {
        return if (level <= WARN) android.util.Log.w(tag, msg) else -1
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        return if (level <= WARN) android.util.Log.w(tag, msg, tr) else -1
    }

    fun isLoggable(var0: String, var1: Int): Boolean {
        return android.util.Log.isLoggable(var0, var1)
    }

    fun w(tag: String, tr: Throwable): Int {
        return if (level <= WARN) android.util.Log.w(tag, tr) else -1
    }

    fun e(tag: String, msg: String): Int {
        return if (level <= ERROR) android.util.Log.e(tag, msg) else -1
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        return if (level <= ERROR) android.util.Log.e(tag, msg, tr) else -1
    }

    fun wtf(tag: String, msg: String): Int {
        return android.util.Log.wtf(tag, msg)
    }

    fun wtf(tag: String, tr: Throwable): Int {
        return android.util.Log.wtf(tag, tr)
    }

    fun wtf(tag: String, msg: String, tr: Throwable): Int {
        return android.util.Log.wtf(tag, msg, tr)
    }

    fun getStackTraceString(tr: Throwable): String {
        return android.util.Log.getStackTraceString(tr)
    }

    fun println(priority: Int, tag: String, msg: String): Int {
        return android.util.Log.println(priority, tag, msg)
    }
}
