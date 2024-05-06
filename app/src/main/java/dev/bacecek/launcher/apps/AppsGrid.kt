package dev.bacecek.launcher.apps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bacecek.launcher.ui.BounceEdgeEffect
import dev.bacecek.launcher.ui.fadingEdges
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsGrid(
    contentPadding: PaddingValues,
    apps: List<AppInfo>,
    gridSize: Int,
    modifier: Modifier = Modifier,
    onAppClicked: (AppInfo) -> Unit = {},
    onAppLongClicked: (AppInfo) -> Unit = {},
) = BounceEdgeEffect {
    val scrollState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = scrollState.canScrollBackward) {
        coroutineScope.launch { scrollState.animateScrollToItem(0) }
    }
    LazyVerticalGrid(
        contentPadding = contentPadding,
        columns = GridCells.Fixed(gridSize),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        state = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .fadingEdges(
                scrollState = scrollState,
                topEdgeHeight = contentPadding.calculateTopPadding(),
                bottomEdgeHeight = contentPadding.calculateBottomPadding(),
            )
            .then(modifier)
    ) {
        items(
            apps,
            key = { it.component },
        ) {
            AppIcon(
                modifier = Modifier.animateItemPlacement(),
                appInfo = it,
                onAppClicked = onAppClicked,
                onAppLongClicked = onAppLongClicked,
                isTitleVisible = true,
            )
        }
    }
}
