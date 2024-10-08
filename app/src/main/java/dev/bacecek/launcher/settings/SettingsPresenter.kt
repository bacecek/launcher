package dev.bacecek.launcher.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.bacecek.launcher.make_default.DefaultLauncherRepository
import dev.bacecek.launcher.make_default.MakeDefaultScreen
import kotlinx.coroutines.launch

class SettingsPresenter(
    private val repository: SettingsRepository,
    private val navigator: Navigator,
    private val defaultLauncherRepository: DefaultLauncherRepository,
) : Presenter<SettingsScreen.State> {
    @Composable
    override fun present(): SettingsScreen.State {
        val isLauncherDefault by defaultLauncherRepository.isDefault.collectAsState()
        val gridSize by repository.gridSize.collectAsState()
        val isRecentsEnabled by repository.isRecentsEnabled.collectAsState()
        val showTitle by repository.showTitle.collectAsState()
        val scope = rememberStableCoroutineScope()
        return SettingsScreen.State(
            isMakeDefaultAvailable = !isLauncherDefault,
            gridSize = gridSize,
            availableGridSizes = listOf(3, 4, 5),
            isRecentsEnabled = isRecentsEnabled,
            showTitle = showTitle,
            eventSink = { event ->
                when (event) {
                    is SettingsScreen.Event.GridSizeChosen -> {
                        scope.launch { repository.setGridSize(event.newSize) }
                    }
                    is SettingsScreen.Event.RecentsEnabledChanged -> {
                        scope.launch { repository.setRecentsEnabled(event.enabled) }
                    }
                    is SettingsScreen.Event.ShowTitleChanged -> {
                        scope.launch { repository.setShowTitle(event.enabled) }
                    }
                    is SettingsScreen.Event.BackClicked -> navigator.pop()
                    is SettingsScreen.Event.MakeDefaultClicked -> navigator.goTo(MakeDefaultScreen())
                }
            },
        )
    }

    class Factory(
        private val repository: SettingsRepository,
        private val defaultLauncherRepository: DefaultLauncherRepository,
    ) : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext
        ): Presenter<SettingsScreen.State>? {
            if (screen !is SettingsScreen) {
                return null
            }
            return SettingsPresenter(
                repository = repository,
                navigator = navigator,
                defaultLauncherRepository = defaultLauncherRepository,
            )
        }
    }
}
