package com.appblocker.blocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * No action needed beyond existing: the schedule lives in SharedPreferences
 * and BlockerPrefs computes block/unblock purely from the current date/time,
 * so it automatically continues correctly after a reboot as long as the
 * Accessibility Service is still enabled by the user in system settings.
 * This receiver exists so the OS wakes the app briefly after boot if you
 * want to add reboot-specific logic later (e.g. a reminder notification).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Intentionally minimal.
    }
}
