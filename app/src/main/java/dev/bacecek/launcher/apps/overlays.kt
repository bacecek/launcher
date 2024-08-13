package dev.bacecek.launcher.apps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuitx.overlays.BasicDialogOverlay

@Composable
fun LauncherOverlay(state: AppsScreen.State) {
    val overlay = state.overlay ?: return
    when (overlay) {
        is AppsScreen.Overlay.AppActions -> AppInfoDialog(
            overlay = overlay,
            appInfo = overlay.appInfo,
            eventSink = state.eventSink,
        )
        is AppsScreen.Overlay.LauncherMenu -> LauncherMenuDialog(
            overlay = overlay,
            eventSink = state.eventSink,
        )
    }
}

@Composable
private fun AppInfoDialog(
    overlay: AppsScreen.Overlay,
    appInfo: AppInfo,
    eventSink: (AppsScreen.Event) -> Unit,
) = LauncherDialog(
    overlay = overlay,
    items = buildList {
        add("Info" to AppsScreen.Event.AppInfoClicked(appInfo))
        if (!appInfo.isSystemApp) {
            add("Uninstall" to AppsScreen.Event.AppUninstallClicked(appInfo))
        }
    }, eventSink = eventSink
)

@Composable
private fun LauncherMenuDialog(
    overlay: AppsScreen.Overlay,
    eventSink: (AppsScreen.Event) -> Unit,
) = LauncherDialog(
    overlay = overlay,
    items = buildList {
        add("Wallpaper & style" to AppsScreen.Event.WallpaperAndStyleClicked)
        add("Settings" to AppsScreen.Event.SettingsClicked)
    }, eventSink = eventSink
)

@Composable
private fun LauncherDialog(
    overlay: AppsScreen.Overlay,
    items: List<Pair<String, AppsScreen.Event>>,
    eventSink: (AppsScreen.Event) -> Unit,
) {
    OverlayEffect(overlay) {
        show(BasicDialogOverlay(
            model = Unit,
            onDismissRequest = { eventSink(AppsScreen.Event.OverlayDismissed) },
        ) { _, navigator ->
            Card(
                shape = RoundedCornerShape(16.dp),
            ) {
                LazyColumn {
                    items(items) { (text, event) ->
                        AppInfoDialogButton(text = text, onClick = {
                            eventSink(event)
                            navigator.finish(Unit)
                        })
                    }
                }
            }
        })
    }
}

@Composable
private fun AppInfoDialogButton(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(text = text)
    }
}
