package com.opendialer.app.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class CallAccessibilityRecorderService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitors window state during active call for optional stream routing
    }

    override fun onInterrupt() {}
}
