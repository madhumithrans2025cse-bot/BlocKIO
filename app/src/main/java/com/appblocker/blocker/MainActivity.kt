package com.appblocker.blocker

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var timePicker: TimePicker
    private lateinit var statusText: TextView
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerApps)
        timePicker = findViewById(R.id.timePicker)
        statusText = findViewById(R.id.statusText)
        timePicker.setIs24HourView(true)

        val apps = loadInstallableApps()
        val previouslySelected = BlockerPrefs.getBlockedPackages(this)
        apps.forEach { it.isSelected = previouslySelected.contains(it.packageName) }

        adapter = AppListAdapter(apps)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        if (BlockerPrefs.isScheduleActive(this)) {
            timePicker.hour = BlockerPrefs.getUnblockHour(this)
            timePicker.minute = BlockerPrefs.getUnblockMinute(this)
        }

        findViewById<android.widget.Button>(R.id.btnStart).setOnClickListener {
            startSchedule()
        }
        findViewById<android.widget.Button>(R.id.btnStop).setOnClickListener {
            BlockerPrefs.stopSchedule(this)
            updateStatus()
            Toast.makeText(this, "Blocking schedule stopped", Toast.LENGTH_SHORT).show()
        }
        findViewById<android.widget.Button>(R.id.btnAccessibilitySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun startSchedule() {
        val selected = adapter.getSelectedPackages()
        if (selected.isEmpty()) {
            Toast.makeText(this, "Select at least one app to block", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(
                this,
                "Enable App Blocker in Accessibility settings first",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }

        BlockerPrefs.saveSchedule(this, selected, timePicker.hour, timePicker.minute)
        updateStatus()
        Toast.makeText(
            this,
            "Started. Selected apps blocked daily until %02d:%02d for 30 days."
                .format(timePicker.hour, timePicker.minute),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateStatus() {
        if (BlockerPrefs.isScheduleActive(this)) {
            val day = BlockerPrefs.getDayNumber(this)
            val remaining = BlockerPrefs.getDaysRemaining(this)
            val hour = BlockerPrefs.getUnblockHour(this)
            val minute = BlockerPrefs.getUnblockMinute(this)
            statusText.text = "Active — Day %d of %d (%d days remaining)\nBlocked apps unlock daily at %02d:%02d"
                .format(day, BlockerPrefs.getTotalDays(this), remaining, hour, minute)
        } else {
            statusText.text = "No active schedule. Select apps, set a time, and tap Start."
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = "$packageName/${AppBlockerService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedComponent, ignoreCase = true)) return true
        }
        return false
    }

    private fun loadInstallableApps(): MutableList<AppInfo> {
        val pm = packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val result = mutableListOf<AppInfo>()
        for (info in installed) {
            // Skip our own app and apps without a launcher entry (system components)
            if (info.packageName == packageName) continue
            val launchIntent = pm.getLaunchIntentForPackage(info.packageName) ?: continue
            val isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystemApp) continue
            result.add(
                AppInfo(
                    packageName = info.packageName,
                    label = pm.getApplicationLabel(info).toString(),
                    icon = pm.getApplicationIcon(info)
                )
            )
        }
        return result.sortedBy { it.label.lowercase() }.toMutableList()
    }
}
