package com.appblocker.blocker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Watches which app comes to the foreground. If that app is in the
 * blocked list and we're still inside today's block window (before the
 * unblock time) and within the 30-day schedule, it launches BlockActivity
 * on top of it instead of letting the user use it.
 */
class AppBlockerService : AccessibilityService() {

    private var lastBlockedPackage: String? = null
    private var lastBlockTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (BlockerPrefs.shouldBlockPackage(this, packageName)) {
            // Avoid re-firing the block screen in a tight loop for the same package
            val now = System.currentTimeMillis()
            if (packageName == lastBlockedPackage && now - lastBlockTime < 800) return
            lastBlockedPackage = packageName
            lastBlockTime = now

            val intent = Intent(this, BlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, packageName)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}
}
