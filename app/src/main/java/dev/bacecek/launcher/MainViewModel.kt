package dev.bacecek.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
class MainViewModel(
    private val context: Context,
) : ViewModel() {
    private val recentsDataSource = RecentUsedDataSource(context)
    private val settingsDataSource = SettingsDataStore(context)

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
            recentsDataSource.onAppUsed(appInfo)
        }
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
                    app.label.toString(),
                    app.getIcon(0),
                    app.applicationInfo.packageName,
                    app.componentName.className,
                    app.user,
                )
            }
            .sortedBy { it.name }
            .toList()
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

    companion object {
        private val FILTERED_COMPONENTS = listOf(
            "com.google.android.googlequicksearchbox/.VoiceSearchActivity",
            "com.google.android.launcher/.StubApp",
            "com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity",
        ).map { ComponentName.unflattenFromString(it) }.toSet()
    }

}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(context.applicationContext) as T
    }
}