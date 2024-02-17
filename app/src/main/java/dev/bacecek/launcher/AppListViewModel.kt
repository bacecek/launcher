package dev.bacecek.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.UserManager
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bacecek.launcher.recent.RecentsDataSource
import dev.bacecek.launcher.settings.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DEFAULT_GRID_SIZE = 4

@Suppress("StaticFieldLeak")
class AppListViewModel(
    private val context: Context,
    private val recentsDataSource: RecentsDataSource,
    settingsDataSource: SettingsDataStore,
) : ViewModel() {

    val gridSize: StateFlow<Int> = settingsDataSource.gridSize.map {
        it ?: DEFAULT_GRID_SIZE
    }.stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_GRID_SIZE)

    val apps: StateFlow<List<AppInfo>> = MutableStateFlow(loadAppList())

    val recents: Flow<List<AppInfo>> = combine(
        recentsDataSource.recentUsedApps,
        apps,
        gridSize
    ) { recents, apps, gridSize ->
        apps.asSequence().map { it to recents[it.packageName] }
            .filter { it.second != null }
            .sortedByDescending { it.second!! }
            .map { it.first }
            .take(gridSize)
            .toList()
    }

    fun onAppClicked(appInfo: AppInfo) {
        launchApp(appInfo)
        viewModelScope.launch {
            recentsDataSource.onAppUsed(appInfo.packageName)
        }
    }

    fun onAppInfoClicked(appInfo: AppInfo) {
        openAppInfo(appInfo)
    }

    fun onAppUninstallClicked(appInfo: AppInfo) {
        uninstall(appInfo)
    }

    private fun loadAppList(): List<AppInfo> {
        val userManager = requireNotNull(context.getSystemService<UserManager>())
        val launcherApps = requireNotNull(context.getSystemService<LauncherApps>())

        return userManager.userProfiles
            .asSequence()
            .flatMap { launcherApps.getActivityList(null, it) }
            .filter { it.applicationInfo.packageName != BuildConfig.APPLICATION_ID }
            .filter { !FILTERED_COMPONENTS.contains(it.componentName) }
            .map { app ->
                AppInfo(
                    name = app.label.toString(),
                    icon = app.getIcon(0),
                    packageName = app.applicationInfo.packageName,
                    activityClassName = app.componentName.className,
                    component = app.componentName,
                    user = app.user,
                    isSystemApp = context.isSystemApp(app.applicationInfo.packageName)
                )
            }
            .sortedBy { it.name }
            .toList()
    }

    private fun Context.isSystemApp(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                    || (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0))
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun launchApp(appInfo: AppInfo) {
        val launcher = requireNotNull(context.getSystemService<LauncherApps>())

        val component = if (appInfo.activityClassName.isNullOrBlank()) {
            val activities = launcher.getActivityList(appInfo.packageName, appInfo.user)
            activities.lastOrNull()?.let {
                ComponentName(appInfo.packageName, it.name)
            }
        } else {
            ComponentName(appInfo.packageName, appInfo.activityClassName)
        }

        component?.let {
            launcher.startMainActivity(it, appInfo.user, null, null)
        }
    }

    private fun uninstall(appInfo: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${appInfo.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openAppInfo(appInfo: AppInfo) {
        val launcher = requireNotNull(context.getSystemService<LauncherApps>())
        launcher.startAppDetailsActivity(appInfo.component, appInfo.user, null, null)
    }

    companion object {
        private val FILTERED_COMPONENTS = listOf(
            "com.google.android.googlequicksearchbox/.VoiceSearchActivity",
            "com.google.android.launcher/.StubApp",
            "com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity",
        ).map { ComponentName.unflattenFromString(it) }.toSet()
    }

}
