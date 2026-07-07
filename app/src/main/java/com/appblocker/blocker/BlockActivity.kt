package com.appblocker.blocker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block)

        val packageName = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE)
        val appLabel = try {
            if (packageName != null) {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, 0)
                ).toString()
            } else "This app"
        } catch (e: Exception) {
            "This app"
        }

        val hour = BlockerPrefs.getUnblockHour(this)
        val minute = BlockerPrefs.getUnblockMinute(this)
        val day = BlockerPrefs.getDayNumber(this)
        val total = BlockerPrefs.getTotalDays(this)

        findViewById<TextView>(R.id.blockTitle).text = "$appLabel is blocked"
        findViewById<TextView>(R.id.blockSubtitle).text =
            "Unlocks today at %02d:%02d  •  Day %d of %d"
                .format(hour, minute, day, total)

        findViewById<Button>(R.id.btnGoHome).setOnClickListener {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(homeIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        // Prevent dismissing the block screen with back button; send home instead.
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(homeIntent)
        finish()
    }
}
