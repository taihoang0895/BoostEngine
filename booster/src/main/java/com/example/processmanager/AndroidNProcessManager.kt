package com.example.processmanager

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.processmanager.process.AndroidProcesses
import com.example.processmanager.process.AppProcess
import com.example.processmanager.process.ProcessInfo
import com.example.processmanager.util.Util

@RequiresApi(Build.VERSION_CODES.N)
internal object AndroidNProcessManager : ProcessManager {
    override suspend fun getRunningProcessManager(context: Context): List<ProcessInfo> {
        val ownerPackageName = context.packageName
        val processMap = HashMap<String, ProcessInfo>()

        AndroidProcesses.getRunningAppProcesses().forEach {
            val processInfo = parseProcessInfo(context, it)
            if (processInfo != null) {
                if (!processMap.containsKey(processInfo.pkgName) && !Util.isSystemApp(context, processInfo)) {
                    processInfo.isSysApp = false
                    processInfo.isOwnerApp = processInfo.pkgName.equals(ownerPackageName)
                    processMap.put(processInfo.pkgName, processInfo)
                }
            }
        }
        processMap.putAll(Util.getRunningService(context))
        return ArrayList<ProcessInfo>(processMap.values)
    }

    private fun parseProcessInfo(context: Context, appProcess: AppProcess): ProcessInfo? {
        try {
            val pkgName = appProcess.getPackageName()
            if (pkgName.isEmpty()) {
                return null
            }
            val memSize = appProcess.statm().getResidentSetSize() / 1024
            val appName = Util.getAppName(context, pkgName)
            return ProcessInfo(pkgName, memSize, appName)
        } catch (e: Exception) {

        }
        return null
    }
}