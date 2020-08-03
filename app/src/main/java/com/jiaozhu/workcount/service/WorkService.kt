package com.jiaozhu.workcount.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.*
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.jiaozhu.workcount.CApplication
import com.jiaozhu.workcount.data.History
import com.jiaozhu.workcount.utils.Log
import com.jiaozhu.workcount.utils.logTag


class WorkService : Service() {
    private val binder = WorkBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class WorkBinder : Binder()

    override fun onCreate() {
        super.onCreate()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {

            override fun onLinkPropertiesChanged(network: Network?, linkProperties: LinkProperties?) {
                val dao = (application as CApplication).db.historyDao()
                val ssid = getWIFISSID()
                if (dao.getLastNode()?.ssid == ssid) return
                dao.insert(History(ssid = ssid))
                Log.i(logTag ?: "", "change:${ssid}")
            }

        })
    }

    /**
     * 获取SSID
     * @param activity 上下文
     * @return  WIFI 的SSID
     */
    @SuppressLint("ObsoleteSdkInt")
    fun getWIFISSID(): String {
        val ssid = "unknown id"

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {

            val mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val info = mWifiManager.connectionInfo

            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                info.ssid
            } else {
                info.ssid.replace("\"", "")
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {

            val connManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.activeNetworkInfo
            if (networkInfo.isConnected) {
                if (networkInfo.extraInfo != null) {
                    return networkInfo.extraInfo.replace("\"", "")
                }
            }
        }
        return ssid
    }

}
