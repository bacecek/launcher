package dev.bacecek.launcher.navigation

import android.content.Context
import android.content.pm.LauncherApps
import com.slack.circuitx.android.AndroidScreen
import com.slack.circuitx.android.AndroidScreenStarter
import com.slack.circuitx.android.IntentScreen
import dev.bacecek.launcher.utils.requireSystemService

class AndroidScreenStarterImpl(
    private val context: Context,
) : AndroidScreenStarter {
    private val launcherApps: LauncherApps by lazy { context.requireSystemService() }

    override fun start(screen: AndroidScreen) {
        when (screen) {
            is LauncherAppsScreen -> screen.startWith(launcherApps)
            is IntentScreen -> context.startActivity(screen.intent, screen.options)
        }
    }
}
