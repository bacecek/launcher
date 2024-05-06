package dev.bacecek.launcher.navigation

import android.content.ComponentName
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Parcelable
import android.os.UserHandle
import com.slack.circuitx.android.AndroidScreen
import kotlinx.parcelize.Parcelize

sealed interface LauncherAppsScreen : AndroidScreen {
    fun startWith(launcherApps: LauncherApps)
}

@Parcelize
data class AppDetailsScreen(
    val componentName: ComponentName,
    val userHandle: UserHandle,
) : LauncherAppsScreen {
    override fun startWith(launcherApps: LauncherApps) {
        launcherApps.startAppDetailsActivity(componentName, userHandle, null, null)
    }
}

@Parcelize
data class AppIntentScreen(
    val info: Info,
) : LauncherAppsScreen {
    override fun startWith(launcherApps: LauncherApps) {
        val component = if (info.activityClassName.isNullOrBlank()) {
            val activities = launcherApps.getActivityList(info.packageName, info.userHandle)
            activities.lastOrNull()?.let { ComponentName(info.packageName, it.name) }
        } else {
            ComponentName(info.packageName, info.activityClassName)
        }

        component?.let {
            launcherApps.startMainActivity(it, info.userHandle, null, null)
        } ?: throw IllegalStateException("Unable to open app $info")
    }

    @Parcelize
    data class Info(
        val activityClassName: String?,
        val packageName: String,
        val userHandle: UserHandle,
    ) : Parcelable
}
