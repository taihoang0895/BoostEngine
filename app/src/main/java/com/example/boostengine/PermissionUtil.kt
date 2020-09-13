package com.example.boostengine

import android.content.Context
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import com.example.boostengine.services.AccessibilityHelperService

object PermissionUtil {
    fun isAccessibilityEnabled(context:Context):Boolean{
        var accessibilityEnabled = 0
        val ACCESSIBILITY_SERVICE =
            context.packageName + "/" + AccessibilityHelperService::class.java.name
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }

        val mStringColonSplitter = SimpleStringSplitter(':')

        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessabilityService = mStringColonSplitter.next()
                    if (accessabilityService.equals(ACCESSIBILITY_SERVICE, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}