package com.example.processmanager.process

import java.io.File
import java.lang.Exception

object AndroidProcesses {
    fun getRunningAppProcesses(): List<AppProcess> {
        val listAppProcess = ArrayList<AppProcess>()
        val files = File("/proc").listFiles()
        files.forEach lit@{ file ->
            if (file.isDirectory) {
                var pid = -1
                try {
                    pid = file.name.toInt()
                    listAppProcess.add(AppProcess(pid))
                } catch (e: Exception) {
                    return@lit
                }
            }
        }
        return listAppProcess
    }


}