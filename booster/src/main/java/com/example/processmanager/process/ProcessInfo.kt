package com.example.processmanager.process

data class ProcessInfo(val pkgName: String, val memSize: Long, val appName: String, var isSysApp:Boolean=false, var isOwnerApp:Boolean=false)