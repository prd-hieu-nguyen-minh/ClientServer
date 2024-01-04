package com.hieuminh.clientserverdemo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SecondService : Service() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: SecondService")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: SecondService")
        stopSelf()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: SecondService")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        val TAG = SecondService::class.java.simpleName
    }
}
