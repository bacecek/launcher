package dev.bacecek.launcher

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.bacecek.launcher.settings.SettingsDialog
import dev.bacecek.launcher.ui.fadingEdges
import dev.bacecek.launcher.ui.theme.ApplicationTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            BackHandler {}
            ApplicationTheme {
                Scaffold(
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Surface(color = Color.Transparent) {
                        AppListScreen(innerPadding)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListScreen(
    innerPadding: PaddingValues,
) {
    val viewModel = koinViewModel<AppListViewModel>()
    val appList by viewModel.apps.collectAsStateWithLifecycle()
    val gridSize by viewModel.gridSize.collectAsStateWithLifecycle()
    val recents by viewModel.recents.collectAsStateWithLifecycle()
    val showRecents by remember { derivedStateOf { recents.isNotEmpty() } }

    var showMenuDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val layoutDirection = LocalLayoutDirection.current

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
            bottom = if (showRecents) 16.dp else innerPadding.calculateBottomPadding() + 32.dp
        )
        AppsGrid(
            contentPadding = contentPadding,
            apps = appList,
            gridSize = gridSize,
            onAppClicked = { viewModel.onAppClicked(it) },
            onAppInfoClicked = { viewModel.onAppInfoClicked(it) },
            onAppUninstallClicked = { viewModel.onAppUninstallClicked(it) },
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onLongClick = { showMenuDialog = true },
                    onClick = {},
                )
        )
        if (showRecents) {
            RecentApps(
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                recents = recents,
                gridSize = gridSize,
                onAppClicked = { viewModel.onAppClicked(it) },
                onAppInfoClicked = { viewModel.onAppInfoClicked(it) },
                onAppUninstallClicked = { viewModel.onAppUninstallClicked(it) },
            )
        }
    }

    if (showMenuDialog) {
        LauncherMenuDialog(
            onDismissRequest = { showMenuDialog = false },
            onWallpaperAndStyleClicked = {
                showMenuDialog = false
                viewModel.onWallpaperAndStyleClicked()
            },
            onSettingsClicked = {
                showMenuDialog = false
                showSettingsDialog = true
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismissRequest = { showSettingsDialog = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsGrid(
    contentPadding: PaddingValues,
    apps: List<AppInfo>,
    gridSize: Int,
    modifier: Modifier,
    onAppClicked: (AppInfo) -> Unit,
    onAppUninstallClicked: (AppInfo) -> Unit,
    onAppInfoClicked: (AppInfo) -> Unit,
) {
    val scrollState = rememberLazyGridState()
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
            App(
                modifier = Modifier.animateItemPlacement(),
                appInfo = it,
                onAppClicked = onAppClicked,
                onAppInfoClicked = onAppInfoClicked,
                onAppUninstallClicked = onAppUninstallClicked,
                isTitleVisible = true,
            )
        }
    }
}

@Composable
fun RecentApps(
    modifier: Modifier = Modifier,
    recents: List<AppInfo>,
    gridSize: Int,
    onAppClicked: (AppInfo) -> Unit,
    onAppUninstallClicked: (AppInfo) -> Unit,
    onAppInfoClicked: (AppInfo) -> Unit,
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
                App(
                    modifier = Modifier.fillParentMaxWidth(1f / gridSize),
                    appInfo = it,
                    onAppClicked = onAppClicked,
                    onAppInfoClicked = onAppInfoClicked,
                    onAppUninstallClicked = onAppUninstallClicked,
                    isTitleVisible = false,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun App(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    onAppClicked: (AppInfo) -> Unit,
    onAppUninstallClicked: (AppInfo) -> Unit,
    onAppInfoClicked: (AppInfo) -> Unit,
    isTitleVisible: Boolean,
) {
    var showAppInfo by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .combinedClickable(
                onLongClick = { showAppInfo = true },
                onClick = { onAppClicked(appInfo) },
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            )
            .then(modifier)
    ) {
        Image(
            painter = rememberDrawablePainter(drawable = appInfo.icon),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Crop,
        )
        if (isTitleVisible) {
            Spacer(modifier = Modifier.height(8.dp))
            AppName(appInfo.name)
        }
    }
    if (showAppInfo) {
        AppInfoTooltip(
            appInfo,
            onAppInfoClicked = {
                showAppInfo = false
                onAppInfoClicked(appInfo)
            },
            onAppUninstallClicked = {
                showAppInfo = false
                onAppUninstallClicked(appInfo)
            },
            onDismiss = { showAppInfo = false },
        )
    }
}

@Composable
fun AppName(name: String) {
    Text(
        text = name,
        textAlign = TextAlign.Center,
        minLines = 2,
        maxLines = 2,
        lineHeight = 14.sp,
        fontSize = 12.sp,
        color = Color.White,
        style = LocalTextStyle.current.copy(
            shadow = Shadow(
                offset = Offset(0f, 2f),
                blurRadius = 5f,
            )
        )
    )
}

@Composable
fun AppInfoTooltip(
    appInfo: AppInfo,
    onAppInfoClicked: () -> Unit,
    onAppUninstallClicked: () -> Unit,
    onDismiss: () -> Unit,
) = LauncherDialog(items = buildList {
    add("Info" to onAppInfoClicked)
    if (!appInfo.isSystemApp) {
        add("Uninstall" to onAppUninstallClicked)
    }
}, onDismissRequest = onDismiss)

@Composable
fun LauncherMenuDialog(
    onDismissRequest: () -> Unit,
    onWallpaperAndStyleClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) = LauncherDialog(items = buildList {
    add("Wallpaper & style" to onWallpaperAndStyleClicked)
    add("Settings" to onSettingsClicked)
}, onDismissRequest = onDismissRequest)

@Composable
fun LauncherDialog(
    items: List<Pair<String, () -> Unit>>,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
        ) {
            LazyColumn {
                items(items) { (text, onClick) ->
                    AppInfoDialogButton(text = text, onClick = onClick)
                }
            }
        }
    }
}

@Composable
fun AppInfoDialogButton(
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
