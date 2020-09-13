package com.example.processmanager.accessibility

import android.annotation.TargetApi
import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo

@TargetApi(14)
class ForceStopAccessibilityAPI14(val context: Context) :
    ForceStopAccessibility() {
    override fun findForceStopButton(accessibilityNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return AccessibilityUtils.findAccessibilityNodeInfosByText(
            accessibilityNodeInfo,
            AccessibilityUtils.findLocalStringById(
                context,
                AccessibilityConstant.FORCE_STOP_BUTTON_STRING_ID,
                AccessibilityConstant.SETTING_PACKAGE_NAME
            )
        )
    }

    override fun findConfirmButton(accessibilityNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return AccessibilityUtils.findAccessibilityNodeInfosByText(
            accessibilityNodeInfo,
            AccessibilityUtils.findLocalStringById(
                context,
                AccessibilityConstant.CONFIRM_BUTTON_STRING_ID,
                AccessibilityConstant.SETTING_PACKAGE_NAME
            )
        )
    }
}