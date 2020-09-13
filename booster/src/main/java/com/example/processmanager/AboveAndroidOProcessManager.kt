package com.example.processmanager

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import com.example.processmanager.process.ProcessInfo
import com.example.processmanager.util.Util
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
internal object AboveAndroidOProcessManager : ProcessManager {
    override suspend fun getRunningProcessManager(context: Context): List<ProcessInfo> {
        val listRunningProcess = ArrayList<ProcessInfo>()
        val packageManager = context.packageManager

        val listInstalledPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val packetLockedSet = HashSet<String>()
        listInstalledPackages.forEach {
            if (!packetLockedSet.contains(it.packageName)) {
                if ((it.applicationInfo.flags and ApplicationInfo.FLAG_STOPPED) == 0 && Util.isLaunchable(packageManager, it.packageName) && !Util.isSystemApp(it.applicationInfo)) {
                    val pkgName = it.packageName
                    val memSize = getMemSize(context, pkgName)
                    val appName = Util.getAppName(context, pkgName)
                    val processInfo = ProcessInfo(pkgName, memSize, appName)
                    processInfo.isSysApp = false
                    processInfo.isOwnerApp = processInfo.pkgName.equals(context.packageName)
                    listRunningProcess.add(processInfo)

                }
            }

        }
        return listRunningProcess
    }

    private fun getMemSize(appContext: Context, pkgName: String): Long {
        val storageStatsManager: StorageStatsManager = appContext.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val storageManager: StorageManager = appContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        val storageVolumes = storageManager.storageVolumes
        val user = Process.myUserHandle()


        var cacheSize: Long = 0
        for (storageVolume in storageVolumes) {
            try {
                val uuidStr = storageVolume.uuid
                val uuid = if (uuidStr == null) StorageManager.UUID_DEFAULT else UUID.fromString(uuidStr)
                val storageStats = storageStatsManager.queryStatsForPackage(uuid, pkgName, user)
                cacheSize += storageStats.cacheBytes
            } catch (e: Exception) {

            }

        }
        return cacheSize
    }
}