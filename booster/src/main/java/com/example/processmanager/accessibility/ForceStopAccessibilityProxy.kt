package com.example.processmanager.accessibility

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

object ForceStopAccessibilityProxy {
    private lateinit var accessibility: ForceStopAccessibility

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= 18) {
            accessibility = ForceStopAccessibilityAPI18(context)
        }else{
            accessibility = ForceStopAccessibilityAPI14(context)
        }
    }

    fun findForceStopButton(accessibilityNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return accessibility.findForceStopButton(accessibilityNodeInfo)
    }
    fun isInstalledAppDetailsTop(accessibilityEvent: AccessibilityEvent?): Boolean {
        return accessibility.isInstalledAppDetailsTop(accessibilityEvent!!)
    }
    fun findConfirmButton(accessibilityNodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        return accessibility.findConfirmButton(accessibilityNodeInfo!!)
    }
    fun isAlertDialog(accessibilityEvent: AccessibilityEvent?): Boolean {
        return accessibility.isAlertDialog(accessibilityEvent!!)
    }
}