package com.example.processmanager

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.MutableLiveData
import com.example.processmanager.accessibility.ForceStopAccessibilityProxy

enum class Status {
    WAITING_FOR_CLICKED_ON_FORCE_BUTTON,
    WAITING_FOR_SHOW_CONFIRM_DIALOG,
    DONE
}

data class ForceProcessInfo(val pkgName: String, var status: Status)
interface ForceKillAppHandler {
    fun getForceProcessInfo(): MutableLiveData<ForceProcessInfo> {
        return CoreProcessManager.getForceProcessInfo()
    }

    fun handelAccessibilityEvent(event: AccessibilityEvent) {
        val forceProcessInfo = getForceProcessInfo().value
        if (forceProcessInfo == null) {
            return
        }

        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.eventType) {
            if (ForceStopAccessibilityProxy.isInstalledAppDetailsTop(event)) {
                val forceButton = ForceStopAccessibilityProxy.findForceStopButton(event.source)
                if (forceButton != null) {
                    if (forceButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        // maybe show confirm dialog
                        getForceProcessInfo().postValue(
                            ForceProcessInfo(
                                forceProcessInfo.pkgName,
                                Status.WAITING_FOR_SHOW_CONFIRM_DIALOG
                            )
                        )
                    }

                }
            } else if (ForceStopAccessibilityProxy.isAlertDialog(event) && forceProcessInfo.status == Status.WAITING_FOR_SHOW_CONFIRM_DIALOG) {
                val confirmButton = ForceStopAccessibilityProxy.findConfirmButton(event.source)
                if (confirmButton != null) {
                    if (confirmButton.isEnabled && confirmButton.isClickable) {
                        if (confirmButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                            getForceProcessInfo().postValue(
                                ForceProcessInfo(
                                    forceProcessInfo.pkgName,
                                    Status.DONE
                                )
                            )
                        }
                    }

                }

            }

        }
    }
}