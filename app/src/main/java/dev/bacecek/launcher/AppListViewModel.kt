package dev.bacecek.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bacecek.launcher.di.CoroutineDispatchers
import dev.bacecek.launcher.recent.RecentsDataSource
import dev.bacecek.launcher.settings.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppListViewModel(
    private val launcherAppsFacade: LauncherAppsFacade,
    private val recentsDataSource: RecentsDataSource,
    private val settingsDataSource: SettingsDataStore,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    init {
        viewModelScope.launch(dispatchers.io) {
            launcherAppsFacade.onAppRemoved.receiveAsFlow().collect {
                recentsDataSource.onAppRemoved(it.component)
            }
        }
    }

    val gridSize: StateFlow<Int>
        get() = settingsDataSource.gridSize

    val apps: StateFlow<List<AppInfo>> = launcherAppsFacade.apps.map {
        it.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val recents: StateFlow<List<AppInfo>> = combine(
        recentsDataSource.recentUsedApps,
        apps,
        gridSize
    ) { recents, apps, gridSize ->
        println("recents")
        apps.asSequence().map { it to recents[it.component] }
            .filter { it.second != null }
            .sortedByDescending { it.second!! }
            .map { it.first }
            .take(gridSize)
            .toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val showRecents: StateFlow<Boolean> = settingsDataSource.isRecentsEnabled

    fun onAppClicked(appInfo: AppInfo) {
        launcherAppsFacade.launchApp(appInfo)
        viewModelScope.launch(dispatchers.io) {
            recentsDataSource.onAppUsed(appInfo.component)
        }
    }

    fun onAppInfoClicked(appInfo: AppInfo) {
        launcherAppsFacade.openAppInfo(appInfo)
    }

    fun onAppUninstallClicked(appInfo: AppInfo) {
        launcherAppsFacade.uninstall(appInfo)
    }

    fun onWallpaperAndStyleClicked() {
        launcherAppsFacade.openWallpaperPicker()
    }

}
