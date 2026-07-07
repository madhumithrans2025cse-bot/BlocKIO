package com.appblocker.blocker

import android.content.Context
import java.util.Calendar

/**
 * Stores the blocking schedule:
 *  - which app package names are blocked
 *  - a single daily "unblock time" (hour:minute) the user set once
 *  - the date the schedule started
 *  - total number of days the schedule runs (30)
 *
 * Rule: on any day within [startDate, startDate + totalDays - 1],
 * a selected app is BLOCKED from midnight until the unblock time,
 * and free to use for the rest of that day. This repeats automatically
 * every day for 30 days without the user re-entering anything.
 */
object BlockerPrefs {

    private const val PREFS_NAME = "app_blocker_prefs"
    private const val KEY_BLOCKED_APPS = "blocked_apps"
    private const val KEY_UNBLOCK_HOUR = "unblock_hour"
    private const val KEY_UNBLOCK_MINUTE = "unblock_minute"
    private const val KEY_START_DAY_EPOCH = "start_day_epoch"
    private const val KEY_TOTAL_DAYS = "total_days"
    private const val KEY_IS_ACTIVE = "is_active"

    const val DEFAULT_TOTAL_DAYS = 30

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSchedule(
        context: Context,
        blockedPackages: Set<String>,
        hour: Int,
        minute: Int
    ) {
        val today = todayEpochDay()
        prefs(context).edit()
            .putStringSet(KEY_BLOCKED_APPS, blockedPackages)
            .putInt(KEY_UNBLOCK_HOUR, hour)
            .putInt(KEY_UNBLOCK_MINUTE, minute)
            .putLong(KEY_START_DAY_EPOCH, today)
            .putInt(KEY_TOTAL_DAYS, DEFAULT_TOTAL_DAYS)
            .putBoolean(KEY_IS_ACTIVE, true)
            .apply()
    }

    fun stopSchedule(context: Context) {
        prefs(context).edit().putBoolean(KEY_IS_ACTIVE, false).apply()
    }

    fun isScheduleActive(context: Context): Boolean {
        val p = prefs(context)
        if (!p.getBoolean(KEY_IS_ACTIVE, false)) return false
        val start = p.getLong(KEY_START_DAY_EPOCH, -1L)
        if (start < 0) return false
        val totalDays = p.getInt(KEY_TOTAL_DAYS, DEFAULT_TOTAL_DAYS)
        val today = todayEpochDay()
        return today in start until (start + totalDays)
    }

    fun getBlockedPackages(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet()

    fun getUnblockHour(context: Context): Int = prefs(context).getInt(KEY_UNBLOCK_HOUR, 18)
    fun getUnblockMinute(context: Context): Int = prefs(context).getInt(KEY_UNBLOCK_MINUTE, 0)

    fun getStartDayEpoch(context: Context): Long = prefs(context).getLong(KEY_START_DAY_EPOCH, -1L)
    fun getTotalDays(context: Context): Int = prefs(context).getInt(KEY_TOTAL_DAYS, DEFAULT_TOTAL_DAYS)

    fun getDayNumber(context: Context): Int {
        val start = getStartDayEpoch(context)
        if (start < 0) return 0
        return (todayEpochDay() - start + 1).toInt().coerceAtLeast(0)
    }

    fun getDaysRemaining(context: Context): Int {
        val total = getTotalDays(context)
        val dayNumber = getDayNumber(context)
        return (total - dayNumber + 1).coerceAtLeast(0)
    }

    /** True if it is currently before today's unblock time. */
    fun isCurrentlyWithinBlockWindow(context: Context): Boolean {
        val cal = Calendar.getInstance()
        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val unblockMinutes = getUnblockHour(context) * 60 + getUnblockMinute(context)
        return nowMinutes < unblockMinutes
    }

    fun shouldBlockPackage(context: Context, packageName: String): Boolean {
        if (packageName == context.packageName) return false
        if (!isScheduleActive(context)) return false
        if (!getBlockedPackages(context).contains(packageName)) return false
        return isCurrentlyWithinBlockWindow(context)
    }

    private fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / (1000L * 60 * 60 * 24)
    }
}
