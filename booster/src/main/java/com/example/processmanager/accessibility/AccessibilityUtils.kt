package com.example.processmanager.accessibility

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityUtils {
    fun isInstalledAppDetailsTop(accessibilityEvent: AccessibilityEvent): Boolean {
        return AccessibilityConstant.APPLICATION_DETAIL_CLASS_NAME.contains(accessibilityEvent.className.toString())
    }

    fun isAlertDialog(accessibilityEvent: AccessibilityEvent): Boolean {
        return AccessibilityConstant.DIALOG_CLASS_NAME.contains(accessibilityEvent.className.toString())
    }

    @TargetApi(18)
    fun findAccessibilityNodeInfoByViewId(accessibilityNodeInfo: AccessibilityNodeInfo, viewIdStr: String): AccessibilityNodeInfo? {
        var accessibilityNodeInfo2: AccessibilityNodeInfo? = null
        val listAccessibilityNodeInfo = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(viewIdStr)

        if (listAccessibilityNodeInfo.size > 0) {
            accessibilityNodeInfo2 = listAccessibilityNodeInfo.removeAt(0)
        }
        return accessibilityNodeInfo2
    }

    @TargetApi(18)
    fun findAccessibilityNodeInfosButtonById(accessibilityNodeInfo: AccessibilityNodeInfo, listViewId: List<String>): AccessibilityNodeInfo? {
        var accessibilityNodeInfo2: AccessibilityNodeInfo? = null
        run loop@{
            listViewId.forEach {
                accessibilityNodeInfo2 = findAccessibilityNodeInfoByViewId(accessibilityNodeInfo, it)
                if (accessibilityNodeInfo2 != null) {
                    return@loop
                }
            }
        }
        return accessibilityNodeInfo2
    }

    fun findAccessibilityNodeInfosByText(accessibilityNodeInfo: AccessibilityNodeInfo, listViewText: List<String>): AccessibilityNodeInfo? {
        var accessibilityNodeInfo2: AccessibilityNodeInfo? = null
        run loop@{
            listViewText.forEach {
                accessibilityNodeInfo2 = findAccessibilityNodeInfoByText(accessibilityNodeInfo, it)
                if (accessibilityNodeInfo2 != null) {
                    return@loop
                }
            }
        }
        return accessibilityNodeInfo2
    }

    fun findAccessibilityNodeInfoByText(accessibilityNodeInfo: AccessibilityNodeInfo, viewText: String): AccessibilityNodeInfo? {
        var accessibilityNodeInfo2: AccessibilityNodeInfo? = null
        val listAccessibilityNodeInfo = accessibilityNodeInfo.findAccessibilityNodeInfosByText(viewText)
        if (listAccessibilityNodeInfo.size > 0) {
            accessibilityNodeInfo2 = listAccessibilityNodeInfo.removeAt(0)
        }
        return accessibilityNodeInfo2
    }

    fun findLocalStringById(context: Context, listViewName: List<String>, pkgName: String): List<String> {
        val listNewViewId = ArrayList<String>()
        val packageContext = createPkgContext(context, pkgName)
        if (packageContext == null) {
            return listNewViewId
        }

        val resources = packageContext.resources
        listViewName.forEach {
            try {
                val temp = getString(resources, it, pkgName)
                if (temp != null) {
                    listNewViewId.add(temp)
                }
            } catch (e: Exception) {

            }

        }
        return listNewViewId
    }

    private fun getString(resources: Resources, name: String, packageName: String): String? {
        val identifier = resources.getIdentifier(name, "string", packageName)
        if (identifier <= 0) {
            return null
        }
        val string = resources.getString(identifier)
        return string
    }

    private fun createPkgContext(context: Context, pkgName: String): Context? {
        if (context.packageName.equals(pkgName)) {
            return context
        }
        try {
            return context.createPackageContext(pkgName, Context.CONTEXT_IGNORE_SECURITY)
        } catch (e: Exception) {
            return null;
        }
    }
}