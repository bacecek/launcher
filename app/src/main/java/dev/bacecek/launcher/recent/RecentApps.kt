package dev.bacecek.launcher.recent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bacecek.launcher.apps.AppIcon
import dev.bacecek.launcher.apps.AppInfo

@Composable
fun RecentApps(
    modifier: Modifier = Modifier,
    recents: List<AppInfo>,
    gridSize: Int,
    onAppClicked: (AppInfo) -> Unit,
    onAppLongClicked: (AppInfo) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .then(modifier),
        userScrollEnabled = false,
    ) {
        items(
            recents,
            key = { it.component },
        ) {
            Box(contentAlignment = Alignment.Center) {
                AppIcon(
                    modifier = Modifier.fillParentMaxWidth(1f / gridSize),
                    appInfo = it,
                    onAppClicked = onAppClicked,
                    onAppLongClicked = onAppLongClicked,
                    isTitleVisible = false,
                )
            }
        }
    }
}
