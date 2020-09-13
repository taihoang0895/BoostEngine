package com.example.processmanager.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

abstract class ForceStopAccessibility {
    abstract fun findForceStopButton(accessibilityNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo?
    abstract fun findConfirmButton(accessibilityNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo?


    fun isInstalledAppDetailsTop(accessibilityEvent: AccessibilityEvent): Boolean {
        return AccessibilityUtils.isInstalledAppDetailsTop(accessibilityEvent)
    }

    fun isAlertDialog(accessibilityEvent: AccessibilityEvent): Boolean {
        return AccessibilityUtils.isAlertDialog(accessibilityEvent)
    }
}