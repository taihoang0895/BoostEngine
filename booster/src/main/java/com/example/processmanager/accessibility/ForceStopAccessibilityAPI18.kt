package com.example.processmanager.accessibility

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo

class ForceStopAccessibilityAPI18(val context: Context) :
    ForceStopAccessibility() {
    private val LEFT_BUTTON_ID = "com.android.settings:id/left_button"
    private val RIGHT_BUTTON_ID = "com.android.settings:id/right_button"
    private val CONTROL_BUTTONS_PANEL_ID =
        "com.android.settings:id/control_buttons_panel"


    override fun findForceStopButton(accessibilityNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var result: AccessibilityNodeInfo? = null
        if (isMX4MODEL()) {
            result = AccessibilityUtils.findAccessibilityNodeInfoByViewId(
                accessibilityNodeInfo,
                RIGHT_BUTTON_ID
            )
        }
        if (result == null) {
            result = AccessibilityUtils.findAccessibilityNodeInfosButtonById(
                accessibilityNodeInfo,
                AccessibilityConstant.FORCE_STOP_BUTTON_FULL_ID
            )
        }
        if (result == null) {
            result = AccessibilityUtils.findAccessibilityNodeInfosByText(
                accessibilityNodeInfo,
                AccessibilityUtils.findLocalStringById(
                    context,
                    AccessibilityConstant.FORCE_STOP_BUTTON_STRING_ID,
                    AccessibilityConstant.SETTING_PACKAGE_NAME
                )
            )
        }
        if (result == null) {
            val control_buttons_panel =
                AccessibilityUtils.findAccessibilityNodeInfoByViewId(
                    accessibilityNodeInfo,
                    CONTROL_BUTTONS_PANEL_ID
                )
            if (control_buttons_panel != null) {
                result = AccessibilityUtils.findAccessibilityNodeInfoByViewId(
                    control_buttons_panel,
                    LEFT_BUTTON_ID
                )
            }
        }
        return result
    }

    override fun findConfirmButton(accessibilityNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var result: AccessibilityNodeInfo? = null
        result = AccessibilityUtils.findAccessibilityNodeInfosButtonById(
            accessibilityNodeInfo,
            AccessibilityConstant.CONFIRM_BUTTON_FULL_ID
        )
        if (result != null) return result

        return AccessibilityUtils.findAccessibilityNodeInfosByText(
            accessibilityNodeInfo,
            AccessibilityUtils.findLocalStringById(
                context,
                AccessibilityConstant.CONFIRM_BUTTON_STRING_ID,
                AccessibilityConstant.SETTING_PACKAGE_NAME
            )
        )
    }

    private fun isMX4MODEL(): Boolean {
        return "MX4" == Build.MODEL
    }
}