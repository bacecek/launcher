@file:OptIn(ExperimentalMaterial3Api::class)

package dev.bacecek.launcher.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference

@Parcelize
data object SettingsScreen : Screen {
    @Stable
    data class State(
        val isMakeDefaultAvailable: Boolean,
        val gridSize: Int,
        val availableGridSizes: List<Int>,
        val isRecentsEnabled: Boolean,
        val showTitle: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event {
        data class GridSizeChosen(val newSize: Int) : Event
        data class RecentsEnabledChanged(val enabled: Boolean) : Event
        data class ShowTitleChanged(val enabled: Boolean) : Event
        data object BackClicked : Event
        data object MakeDefaultClicked : Event
    }
}

@Composable
fun SettingsScreen(
    state: SettingsScreen.State,
    modifier: Modifier = Modifier,
) {
    ProvidePreferenceLocals {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text("Settings")
                    },
                    navigationIcon = {
                        IconButton(onClick = { state.eventSink(SettingsScreen.Event.BackClicked) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
            ) {
                Preference(
                    title = { Text(text = "Make app default in system") },
                    enabled = state.isMakeDefaultAvailable,
                    onClick = { state.eventSink(SettingsScreen.Event.MakeDefaultClicked) },
                )
                ListPreference(
                    value = state.gridSize,
                    values = state.availableGridSizes,
                    title = { Text(text = "Grid Size") },
                    summary = { Text(text = state.gridSize.toString()) },
                    onValueChange = { state.eventSink(SettingsScreen.Event.GridSizeChosen(it)) },
                )
                SwitchPreference(
                    value = state.isRecentsEnabled,
                    onValueChange = { state.eventSink(SettingsScreen.Event.RecentsEnabledChanged(it)) },
                    title = { Text(text = "Show recent used apps") },
                )
                SwitchPreference(
                    value = state.showTitle,
                    onValueChange = { state.eventSink(SettingsScreen.Event.ShowTitleChanged(it)) },
                    title = { Text(text = "Show app label") },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(
        state = SettingsScreen.State(
            isMakeDefaultAvailable = true,
            gridSize = 4,
            availableGridSizes = listOf(3, 4, 5, 6),
            isRecentsEnabled = true,
            showTitle = true,
            eventSink = {},
        ),
    )
}
