package com.example.boostengine.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.processmanager.ForceKillAppHandler
import com.example.processmanager.Status

class AccessibilityHelperService : AccessibilityService(), ForceKillAppHandler {
    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        val forceProcessInfo = getForceProcessInfo().value
        Log.d("taih", " event type -> ${event.eventType}" )
        if (forceProcessInfo != null && forceProcessInfo.status != Status.DONE) {
            handelAccessibilityEvent(event)
        }

    }

    override fun onServiceConnected() {

        super.onServiceConnected()
    }
}