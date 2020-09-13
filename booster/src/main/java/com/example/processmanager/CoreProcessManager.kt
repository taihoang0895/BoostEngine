package com.example.processmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.processmanager.accessibility.ForceStopAccessibilityProxy
import com.example.processmanager.process.ProcessInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile

object CoreProcessManager {
    private lateinit var appContext: Context
    private val processManager: ProcessManager
    private val listTempFiles = arrayOf(
        "/sys/class/thermal/thermal_zone1/temp",
        "/sys/devices/virtual/thermal/thermal_zone1/temp",
        "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
        "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
        "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
        "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
        "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
        "/sys/devices/platform/tegra_tmon/temp1_input",
        "/sys/kernel/debug/tegra_thermal/temp_tj",
        "/sys/devices/platform/s5p-tmu/temperature",
        "/sys/class/hwmon/hwmon0/device/temp1_input",
        "/sys/devices/platform/s5p-tmu/curr_temp",
        "/sys/htc/cpu_temp",
        "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/ext_temperature",
        "/sys/devices/platform/tegra-tsensor/tsensor_temperature",
        "/sys/class/hwmon/hwmon1/device/soc_temp_input",
        "/sys/class/hwmon/hwmon2/device/soc_temp_input",
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/devices/virtual/thermal/thermal_zone0/temp",
        "/sys/devices/platform/s5p-tmu/curr_temp"
    )
    private val foreProcessInfo = MutableLiveData<ForceProcessInfo>()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            processManager = AboveAndroidOProcessManager
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                processManager = AndroidOProcessManager
            } else {
                processManager = AndroidNProcessManager
            }
        }
    }

    fun init(appContext: Context) {
        this.appContext = appContext
        ForceStopAccessibilityProxy.init(appContext)
    }

    suspend fun getRunningProcessManager(): List<ProcessInfo> = withContext(Dispatchers.IO) {
        processManager.getRunningProcessManager(appContext)
    }

    @Synchronized
    suspend fun killApp(processInfo: ProcessInfo) {
        withContext(Dispatchers.Main) {
            var waitingForProcessing = true
            val timeout = 6000
            val startTime = System.currentTimeMillis()

            val forceProcessInfoObserver = object : Observer<ForceProcessInfo> {
                override fun onChanged(info: ForceProcessInfo) {
                    if (info.pkgName.equals(processInfo.pkgName) && info.status == Status.DONE) {
                        waitingForProcessing = false
                    }
                }

            }
            getForceProcessInfo().postValue(ForceProcessInfo(processInfo.pkgName, Status.WAITING_FOR_CLICKED_ON_FORCE_BUTTON))
            getForceProcessInfo().observeForever(forceProcessInfoObserver)
            try {
                if (!startApplicationDetailActivity(processInfo.pkgName)) {
                    waitingForProcessing = false
                }
                while (waitingForProcessing) {
                    delay(500)
                    if ((System.currentTimeMillis() - startTime) > timeout) {
                        waitingForProcessing = false
                    }
                }
            } finally {
                getForceProcessInfo().removeObserver(forceProcessInfoObserver)
            }
        }
    }


    suspend fun getPercentCPUTemp(): Int = withContext(Dispatchers.IO) {
        var percent = 30
        run loop@{
            listTempFiles.forEach {
                var reader: RandomAccessFile? = null
                try {
                    reader = RandomAccessFile(it, "r")
                    var line: String = reader.readLine()
                    var temp = line.toFloat()
                    temp = temp / 1000.0f
                    percent = temp.toInt()
                    return@loop

                } catch (e: Exception) {
                } finally {
                    reader?.close()
                }
            }
        }
        percent
    }

    internal fun getForceProcessInfo(): MutableLiveData<ForceProcessInfo> {
        return foreProcessInfo
    }

    private fun startApplicationDetailActivity(pkgName: String): Boolean {
        val appIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        appIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        appIntent.setData(Uri.parse("package:${pkgName}"))
        try {
            appContext.startActivity(appIntent)
        } catch (e: Exception) {
            return false
        }
        return true
    }

}