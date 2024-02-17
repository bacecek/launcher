package dev.bacecek.launcher

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.drawablepainter.rememberDrawablePainter
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
            ApplicationTheme(dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(start = 24.dp, end = 24.dp, top = 32.dp),
                    ) {
                        AppListScreen()
                    }
                }
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() = Unit
}

@Composable
fun AppListScreen() {
    val viewModel = koinViewModel<AppListViewModel>()
    val appList = viewModel.apps.collectAsState()
    val gridSize = viewModel.gridSize.collectAsState()
    val recents = viewModel.recents.collectAsState(initial = emptyList())

    Column {
        AppsGrid(
            apps = appList,
            gridSize = gridSize,
            onAppClicked = { viewModel.onAppClicked(it) },
            onAppInfoClicked = { viewModel.onAppInfoClicked(it) },
            onAppUninstallClicked = { viewModel.onAppUninstallClicked(it) },
            modifier = Modifier.weight(1f)
        )
        RecentApps(
            recents = recents,
            onAppClicked = { viewModel.onAppClicked(it) },
            onAppInfoClicked = { viewModel.onAppInfoClicked(it) },
            onAppUninstallClicked = { viewModel.onAppUninstallClicked(it) },
        )
    }
}

@Composable
fun AppsGrid(
    apps: State<List<AppInfo>>,
    gridSize: State<Int>,
    modifier: Modifier,
    onAppClicked: (AppInfo) -> Unit,
    onAppUninstallClicked: (AppInfo) -> Unit,
    onAppInfoClicked: (AppInfo) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize.value),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        items(apps.value) {
            App(
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
    recents: State<List<AppInfo>>,
    onAppClicked: (AppInfo) -> Unit,
    onAppUninstallClicked: (AppInfo) -> Unit,
    onAppInfoClicked: (AppInfo) -> Unit,
) {
    HorizontalDivider(color = Color(0x25000000))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
    ) {
        recents.value.forEach {
            App(
                appInfo = it,
                onAppClicked = onAppClicked,
                onAppInfoClicked = onAppInfoClicked,
                onAppUninstallClicked = onAppUninstallClicked,
                isTitleVisible = false,
                modifier = Modifier.weight(1f)
            )
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
    var appInfoDialogState by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .combinedClickable(
                onLongClick = { appInfoDialogState = true },
                onClick = { onAppClicked(appInfo) },
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
            Text(
                text = appInfo.name,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                lineHeight = 14.sp,
                fontSize = 12.sp,
            )
        }
    }
    if (appInfoDialogState) {
        AppInfoTooltip(
            appInfo,
            onAppInfoClicked = {
                appInfoDialogState = false
                onAppInfoClicked(appInfo)
            },
            onAppUninstallClicked = {
                appInfoDialogState = false
                onAppUninstallClicked(appInfo)
            },
            onDismiss = { appInfoDialogState = false },
        )
    }
}

@Composable
fun AppInfoTooltip(
    appInfo: AppInfo,
    onAppInfoClicked: () -> Unit,
    onAppUninstallClicked: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column {
                AppInfoDialogButton(text = "App info", onClick = onAppInfoClicked)
                if (!appInfo.isSystemApp) {
                    AppInfoDialogButton(text = "Uninstall", onClick = onAppUninstallClicked)
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
