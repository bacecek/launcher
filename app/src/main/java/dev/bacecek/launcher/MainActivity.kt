package dev.bacecek.launcher

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.UserHandle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.bacecek.launcher.ui.theme.ApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>(
        factoryProducer = { MainViewModelFactory(this.applicationContext) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier
                        .padding(innerPadding)
                        .padding(start = 24.dp, end = 24.dp, top = 32.dp)
                    ) {
                        AppListScreen(viewModel)
                    }
                }
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() = Unit
}

@Composable
fun AppListScreen(
    viewModel: MainViewModel,
) {
    val appList = viewModel.apps.collectAsState()
    val gridSize = viewModel.gridSize.collectAsState()
    val recents = viewModel.recents.collectAsState(initial = emptyList())

    Column {
        AppsGrid(
            apps = appList,
            gridSize = gridSize,
            onAppClicked = { viewModel.onAppClicked(it) },
            modifier = Modifier.weight(1f)
        )
        RecentApps(
            recents = recents,
            onAppClicked = { viewModel.onAppClicked(it) },
        )
    }
}

@Composable
fun AppsGrid(
    apps: State<List<AppInfo>>,
    gridSize: State<Int>,
    modifier: Modifier,
    onAppClicked: (AppInfo) -> Unit = {},
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
                isTitleVisible = true,
            )
        }
    }
}

@Composable
fun RecentApps(
    recents: State<List<AppInfo>>,
    onAppClicked: (AppInfo) -> Unit = {},
) {
    Divider()
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
    ) {
        recents.value.forEach {
            App(appInfo = it,
                onAppClicked = onAppClicked,
                isTitleVisible = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun App(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    onAppClicked: (AppInfo) -> Unit = {},
    isTitleVisible: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable {
                onAppClicked(appInfo)
            }
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
}

@Stable
data class AppInfo(
    val name: String,
    val icon: Drawable?,
    val packageName: String,
    val activityClassName: String?,
    val user: UserHandle,
)
