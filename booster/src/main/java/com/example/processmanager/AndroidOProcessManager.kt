package com.example.processmanager

import android.content.Context
import com.example.processmanager.process.ProcessInfo
import com.example.processmanager.util.Util
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Matcher
import java.util.regex.Pattern

internal object AndroidOProcessManager : ProcessManager {
    const val MATCH_PATTERN_STRING = "^.*\\s(\\d+)K.*?\\s+([\\w\\.]+)(:.*?){0,1}$"
    override suspend fun getRunningProcessManager(context: Context): List<ProcessInfo> {
        val ownerPackageName = context.packageName
        val pattern = Pattern.compile(MATCH_PATTERN_STRING)
        val processMap = HashMap<String, ProcessInfo>()
        val command = "top -n 1 -m 20 -s rss"
        try {
            val process = Runtime.getRuntime().exec(command)
            val bufferReader = BufferedReader(InputStreamReader(process.inputStream))
            var infoRangeEntered = false
            var line = bufferReader.readLine()
            while (line != null) {
                if (!infoRangeEntered && line.contains("PID")) {
                    infoRangeEntered = true
                    continue
                }
                if (infoRangeEntered) {
                    val matcher = pattern.matcher(line)
                    if (matcher.find()) {
                        val processInfo = parseProcessInfo(context, matcher)
                        if (processInfo != null && !processInfo.pkgName.isEmpty() && !processMap.containsKey(processInfo.pkgName) && ! Util.isSystemApp(context, processInfo)) {
                            processInfo.isSysApp = false
                            processInfo.isOwnerApp = processInfo.pkgName.equals(ownerPackageName)
                            processMap.put(processInfo.pkgName, processInfo)
                        }
                    }
                }
                process.waitFor()
                line = bufferReader.readLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        processMap.putAll(Util.getRunningService(context))
        return ArrayList<ProcessInfo>(processMap.values)
    }

    private fun parseProcessInfo(context: Context, matcher: Matcher): ProcessInfo? {
        try {
            val packageName: String = matcher.group(2) ?: ""
            if (packageName.isEmpty()) {
                return null
            }
            val memSize: Long = matcher.group(1)?.toLong() ?: 0
            val appName: String = Util.getAppName(context, packageName)
            return ProcessInfo(packageName, memSize, appName)
        } catch (e: Exception) {

        }
        return null
    }


}