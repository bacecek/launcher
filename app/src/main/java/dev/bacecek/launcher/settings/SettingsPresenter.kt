package dev.bacecek.launcher.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch

class SettingsPresenter(
    private val repository: SettingsRepository,
    private val navigator: Navigator,
) : Presenter<SettingsScreen.State> {
    @Composable
    override fun present(): SettingsScreen.State {
        val gridSize by repository.gridSize.collectAsState()
        val isRecentsEnabled by repository.isRecentsEnabled.collectAsState()
        val scope = rememberStableCoroutineScope()
        return SettingsScreen.State(
            gridSize = gridSize,
            availableGridSizes = listOf(3, 4, 5),
            isRecentsEnabled = isRecentsEnabled,
            eventSink = { event ->
                when (event) {
                    is SettingsScreen.Event.GridSizeChosen -> {
                        scope.launch { repository.setGridSize(event.newSize) }
                    }
                    is SettingsScreen.Event.RecentsEnabledChanged -> {
                        scope.launch { repository.setRecentsEnabled(event.enabled) }
                    }
                    is SettingsScreen.Event.BackClicked -> navigator.pop()
                }
            },
        )
    }

    class Factory(
        private val repository: SettingsRepository,
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
            )
        }
    }
}
