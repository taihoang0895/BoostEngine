package com.example.processmanager.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.example.processmanager.process.ProcessInfo
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object Util {

    fun isSystemApp(context: Context, process: ProcessInfo): Boolean {
        val pm = context.packageManager
        return try {
            val applicationInfo = pm.getApplicationInfo(process.pkgName, PackageManager.GET_META_DATA)
            return isSystemApp(applicationInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            true
        }
    }

    fun getAppName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo) as String
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun isInputApp(context: Context, pkgName: String): Boolean {
        val arrayList = ArrayList<String>()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).inputMethodList.forEach {
            arrayList.add(it.packageName)
        }
        return arrayList.contains(pkgName)

    }

    fun isAlarmApp(pkgName: String): Boolean {
        return (pkgName.toLowerCase(Locale.ENGLISH)
            .contains("clock") || pkgName.toLowerCase(Locale.ENGLISH).contains("alarm"));
    }

    fun getRunningService(context: Context): HashMap<String, ProcessInfo> {
        val ownerPackageName = context.packageName
        val service = context.getSystemService(Context.ACTIVITY_SERVICE)
        val processMap = HashMap<String, ProcessInfo>()
        if (service != null) {
            val activityManager = service as ActivityManager
            try {
                val runningServiceList = activityManager.getRunningServices(15)
                runningServiceList.forEach {
                    val pkgName = it.service.packageName
                    val appName = getAppName(context, pkgName)
                    val processInfo = ProcessInfo(pkgName, 0, appName)
                    if (!processInfo.pkgName.isEmpty() && !processMap.containsKey(processInfo.pkgName)) {
                        processInfo.isSysApp = isSystemApp(context, processInfo)
                        processInfo.isOwnerApp = processInfo.pkgName.equals(ownerPackageName)
                        processMap.put(processInfo.pkgName, processInfo)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return processMap
    }

    fun isSystemApp(applicationInfo: ApplicationInfo?): Boolean {
        if (Build.DEVICE.contains("huawei") || Build.DEVICE.contains("HUAWEI")) {
            if (applicationInfo == null) {
                return true
            }
            var isSystem = false
            try {
                applicationInfo.javaClass.methods.forEach { method ->
                    method.isAccessible = true
                    if (method.returnType == String::class) {
                        try {
                            Log.d("libDevice", method.name.toString() + ":" + method.invoke(applicationInfo, arrayOfNulls<Any>(0)) as String)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            Log.d("libDevice", "e:" + e.message)
                        }
                    }
                }

                val method2: Method = applicationInfo.javaClass.getMethod("getResourcePath", *arrayOfNulls<Class<*>>(0))

                try {
                    method2.setAccessible(true)
                    val str = method2.invoke(applicationInfo, arrayOfNulls<Any>(0)) as String
                    if (str.startsWith("/system")) {
                        isSystem = true
                    } else {
                        if (str.startsWith("/data")) {
                            isSystem = false
                        }
                        isSystem = true
                    }
                } catch (e2: java.lang.Exception) {
                    e2.printStackTrace()
                    Log.d("libDevice", "err:" + e2.message + " cause:" + e2.cause)
                }
                return isSystem

            } catch (e: NoSuchMethodException) {
                e.printStackTrace();
                return true
            }
        } else {
            if (applicationInfo == null) {
                return true
            }
            return (applicationInfo.flags and 1) > 0
        }
    }

    fun isLaunchable(packageManager: PackageManager, pkgName: String): Boolean {
        return packageManager.getLaunchIntentForPackage(pkgName) != null
    }

    fun isLauncherApp(context: Context, pkgName: String): Boolean {
        if ("com.android.settings".equals(pkgName)) {
            return false
        }
        val intent = Intent("android.intent.action.MAIN", null)
        intent.addCategory("android.intent.category.HOME")
        intent.setPackage(pkgName)
        var list: List<ResolveInfo>? = null
        try {
            list = context.packageManager.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            list = null
        }
        return list != null && list.size > 0
    }
}