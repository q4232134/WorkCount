package com.jiaozhu.workcount.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.*
import android.net.wifi.WifiManager
import android.os.Binder
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
                val wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val info = wifiMgr.connectionInfo
                val dao = (application as CApplication).db.historyDao()
                if (dao.getLastNode().ssid == info.ssid) return
                dao.insert(History(ssid = info.ssid))
                Log.i(logTag ?: "", "change:${info.ssid}")
            }

        })
    }

}
