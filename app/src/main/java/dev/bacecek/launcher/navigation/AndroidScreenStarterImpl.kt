package dev.bacecek.launcher.navigation

import android.content.Context
import android.content.pm.LauncherApps
import com.slack.circuitx.android.AndroidScreen
import com.slack.circuitx.android.AndroidScreenStarter
import com.slack.circuitx.android.IntentScreen
import dev.bacecek.launcher.make_default.MakeDefaultScreen
import dev.bacecek.launcher.utils.requireSystemService

class AndroidScreenStarterImpl(
    private val context: Context,
) : AndroidScreenStarter {
    private val launcherApps: LauncherApps by lazy { context.requireSystemService() }

    override fun start(screen: AndroidScreen): Boolean {
        when (screen) {
            is LauncherAppsScreen -> screen.startWith(launcherApps)
            is MakeDefaultScreen -> screen.startWith(context)
            is IntentScreen -> context.startActivity(screen.intent, screen.options)
        }
        return true
    }
}
