package com.example.boostengine

import android.app.Application
import com.example.processmanager.CoreProcessManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreProcessManager.init(this)
    }
}