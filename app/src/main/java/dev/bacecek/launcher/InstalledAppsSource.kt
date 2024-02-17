package dev.bacecek.launcher

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.UserHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AppInfo(
    val name: String,
    val icon: Drawable?,
    val packageName: String,
    val activityClassName: String?,
    val component: ComponentName,
    val user: UserHandle,
    val isSystemApp: Boolean,
)

interface InstalledAppsSource {
    val apps: StateFlow<List<AppInfo>>
}

internal class InstalledAppsSourceImpl(
    private val launcherAppsFacade: LauncherAppsFacade,
) : InstalledAppsSource {

    override val apps: StateFlow<List<AppInfo>> = MutableStateFlow(loadAppList())

    private fun loadAppList(): List<AppInfo> {
        return launcherAppsFacade.loadInstalledApps()
            .asSequence()
            .filter { it.packageName != BuildConfig.APPLICATION_ID }
            .filter { !FILTERED_COMPONENTS.contains(it.component) }
            .sortedBy { it.name }
            .toList()
    }

    companion object {
        private val FILTERED_COMPONENTS = listOf(
            "com.google.android.googlequicksearchbox/.VoiceSearchActivity",
            "com.google.android.launcher/.StubApp",
            "com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity",
        ).map { ComponentName.unflattenFromString(it) }.toSet()
    }

}
