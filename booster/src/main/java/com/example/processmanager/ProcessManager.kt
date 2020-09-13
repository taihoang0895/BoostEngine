package com.example.processmanager

import android.content.Context
import com.example.processmanager.process.ProcessInfo

internal interface ProcessManager {
    suspend fun getRunningProcessManager(context: Context): List<ProcessInfo>

}