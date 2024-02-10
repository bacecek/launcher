package dev.bacecek.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.bacecek.launcher.ui.theme.ApplicationTheme

class MainActivity : ComponentActivity() {
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
                        AppListScreen()
                    }
                }
            }
        }
    }

    override fun onBackPressed() = Unit
}

@Composable
fun AppListScreen() {
    val context = LocalContext.current
    val appList = context.loadAppList()

    AppsGrid(apps = appList)
}

@Composable
fun AppsGrid(apps: List<AppInfo>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(apps) {
            App(it)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppsGridPreview() {
    val apps = (1..9).map {
        AppInfo(
            name = "app$it",
            icon = LocalContext.current.getDrawable(R.mipmap.ic_launcher),
            packageName = "com.example.app.$it",
            activityClassName = null,
            user = android.os.Process.myUserHandle(),
        )
    }
    AppsGrid(apps = apps)
}

@Composable
fun App(
    appInfo: AppInfo,
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { context.launchApp(appInfo) }
    ) {
        Image(
            painter = rememberDrawablePainter(drawable = appInfo.icon),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Crop,
        )
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

fun Context.launchApp(appInfo: AppInfo) {
    val launcher = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    val component = if (appInfo.activityClassName.isNullOrBlank()) {
        val activities = launcher.getActivityList(appInfo.packageName, appInfo.user)
        activities.lastOrNull()?.let {
            ComponentName(appInfo.packageName, it.name)
        }
    } else {
        ComponentName(appInfo.packageName, appInfo.activityClassName)
    }

    component?.let {
        launcher.startMainActivity(it, appInfo.user, null, null)
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

fun Context.loadAppList(): List<AppInfo> {
    val userManager = getSystemService(Context.USER_SERVICE) as UserManager
    val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    return userManager.userProfiles
        .asSequence()
        .flatMap { launcherApps.getActivityList(null, it) }
        .filter { it.applicationInfo.packageName != BuildConfig.APPLICATION_ID }
        .map { app ->
            AppInfo(
                app.label.toString(),
                app.getIcon(0),
                app.applicationInfo.packageName,
                app.componentName.className,
                app.user,
            )
        }
        .sortedBy { it.name }
        .toList()
}
