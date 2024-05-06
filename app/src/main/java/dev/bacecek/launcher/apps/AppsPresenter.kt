package dev.bacecek.launcher.apps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.bacecek.launcher.apps.AppsScreen.Event
import dev.bacecek.launcher.apps.AppsScreen.Overlay
import dev.bacecek.launcher.di.CoroutineDispatchers
import dev.bacecek.launcher.navigation.AppDetailsScreen
import dev.bacecek.launcher.navigation.AppIntentScreen
import dev.bacecek.launcher.navigation.UninstallAppScreen
import dev.bacecek.launcher.navigation.WallpaperPickerScreen
import dev.bacecek.launcher.recent.RecentsRepository
import dev.bacecek.launcher.settings.SettingsIntentScreen
import dev.bacecek.launcher.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AppsPresenter(
    private val repository: AppsRepository,
    private val recentUsedAppsRepository: RecentsRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: CoroutineDispatchers,
    private val navigator: Navigator,
) : Presenter<AppsScreen.State> {
    @Composable
    override fun present(): AppsScreen.State {
        val apps by repository.apps.collectAsState(initial = emptyList())
        val gridSize by settingsRepository.gridSize.collectAsState()
        val showTitle by settingsRepository.showTitle.collectAsState()
        val recents by collectRecents().collectAsState(initial = emptyList())
        val scope = rememberStableCoroutineScope()
        var overlay by rememberRetained { mutableStateOf<Overlay?>(null) }
        val context = LocalContext.current
        return AppsScreen.State(
            apps = apps,
            recents = recents,
            gridSize = gridSize,
            showTitle = showTitle,
            eventSink = { event ->
                when (event) {
                    is Event.AppClicked -> {
                        overlay = null
                        navigator.goTo(AppIntentScreen(event.appInfo.toStartAppInfo()))
                        scope.launch(dispatchers.io) {
                            recentUsedAppsRepository.markAppUsed(event.appInfo.component)
                        }
                    }
                    is Event.AppLongClicked -> {
                        overlay = Overlay.AppActions(event.appInfo)
                    }
                    is Event.BackgroundLongClicked -> {
                        overlay = Overlay.LauncherMenu
                    }
                    is Event.AppInfoClicked -> {
                        overlay = null
                        navigator.goTo(AppDetailsScreen(event.appInfo.component, event.appInfo.user))
                    }
                    is Event.AppUninstallClicked -> {
                        overlay = null
                        navigator.goTo(UninstallAppScreen(event.appInfo.packageName))
                    }
                    is Event.WallpaperAndStyleClicked -> {
                        overlay = null
                        navigator.goTo(WallpaperPickerScreen())
                    }
                    is Event.SettingsClicked -> {
                        overlay = null
                        navigator.goTo(SettingsIntentScreen(context))
                    }
                    Event.OverlayDismissed -> {
                        overlay = null
                    }
                }
            },
            overlay = overlay,
        )
    }

    private fun collectRecents(): Flow<List<AppInfo>> {
        return combine(
            settingsRepository.isRecentsEnabled,
            recentUsedAppsRepository.recentUsedApps,
            repository.apps,
            settingsRepository.gridSize,
        ) { isRecentsEnabled, recents, apps, gridSize ->
            if (!isRecentsEnabled) {
                return@combine emptyList()
            }
            apps.asSequence().map { it to recents[it.component] }
                .filter { it.second != null }
                .sortedByDescending { it.second }
                .map { it.first }
                .take(gridSize)
                .toList()
        }
    }

    private fun AppInfo.toStartAppInfo() = AppIntentScreen.Info(
        activityClassName = activityClassName,
        packageName = packageName,
        userHandle = user,
    )

    class Factory(
        private val repository: AppsRepository,
        private val recentUsedAppsRepository: RecentsRepository,
        private val settingsRepository: SettingsRepository,
        private val dispatchers: CoroutineDispatchers,
    ) : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext
        ): Presenter<AppsScreen.State>? {
            if (screen !is AppsScreen) {
                return null
            }
            return AppsPresenter(
                repository = repository,
                recentUsedAppsRepository = recentUsedAppsRepository,
                settingsRepository = settingsRepository,
                dispatchers = dispatchers,
                navigator = navigator,
            )
        }
    }
}
