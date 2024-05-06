package dev.bacecek.launcher.apps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.bacecek.launcher.recent.RecentApps
import kotlinx.parcelize.Parcelize

@Parcelize
data object AppsScreen : Screen {
    data class State(
        val apps: List<AppInfo>,
        val recents: List<AppInfo>,
        val gridSize: Int,
        val eventSink: (Event) -> Unit,
        val overlay: Overlay? = null,
    ) : CircuitUiState

    sealed interface Event {
        data class AppClicked(val appInfo: AppInfo) : Event
        data class AppLongClicked(val appInfo: AppInfo): Event
        data object BackgroundLongClicked: Event

        data class AppInfoClicked(val appInfo: AppInfo): Event
        data class AppUninstallClicked(val appInfo: AppInfo): Event
        data object WallpaperAndStyleClicked: Event
        data object SettingsClicked: Event

        data object OverlayDismissed : Event
    }

    sealed interface Overlay {
        data class AppActions(
            val appInfo: AppInfo,
        ) : Overlay
        data object LauncherMenu : Overlay
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsScreen(
    state: AppsScreen.State,
    modifier: Modifier,
) {
    LauncherOverlay(state)
    val layoutDirection = LocalLayoutDirection.current
    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                start = 24.dp + innerPadding.calculateStartPadding(layoutDirection),
                end = 24.dp + innerPadding.calculateEndPadding(layoutDirection),
            ),
        ) {
            val contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 32.dp,
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = if (state.recents.isNotEmpty()) 16.dp else innerPadding.calculateBottomPadding() + 32.dp
            )
            AppsGrid(
                contentPadding = contentPadding,
                apps = state.apps,
                gridSize = state.gridSize,
                onAppClicked = { state.eventSink(AppsScreen.Event.AppClicked(it)) },
                onAppLongClicked = { state.eventSink(AppsScreen.Event.AppLongClicked(it)) },
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onLongClick = { state.eventSink(AppsScreen.Event.BackgroundLongClicked) },
                        onClick = {},
                    )
            )
            if (state.recents.isNotEmpty()) {
                RecentApps(
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                    recents = state.recents,
                    gridSize = state.gridSize,
                    onAppClicked = { state.eventSink(AppsScreen.Event.AppClicked(it)) },
                    onAppLongClicked = { state.eventSink(AppsScreen.Event.AppLongClicked(it)) },
                )
            }
        }
    }
}
