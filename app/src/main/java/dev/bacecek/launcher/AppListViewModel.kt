package dev.bacecek.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bacecek.launcher.recent.RecentsDataSource
import dev.bacecek.launcher.settings.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DEFAULT_GRID_SIZE = 4

class AppListViewModel(
    private val launcherAppsFacade: LauncherAppsFacade,
    private val recentsDataSource: RecentsDataSource,
    settingsDataSource: SettingsDataStore,
    private val installedAppsSource: InstalledAppsSource,
) : ViewModel() {

    val gridSize: StateFlow<Int> = settingsDataSource.gridSize.map {
        it ?: DEFAULT_GRID_SIZE
    }.stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_GRID_SIZE)

    val apps: StateFlow<List<AppInfo>>
        get() = installedAppsSource.apps

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
        launcherAppsFacade.launchApp(appInfo)
        viewModelScope.launch {
            recentsDataSource.onAppUsed(appInfo.packageName)
        }
    }

    fun onAppInfoClicked(appInfo: AppInfo) {
        launcherAppsFacade.openAppInfo(appInfo)
    }

    fun onAppUninstallClicked(appInfo: AppInfo) {
        launcherAppsFacade.uninstall(appInfo)
    }

}
