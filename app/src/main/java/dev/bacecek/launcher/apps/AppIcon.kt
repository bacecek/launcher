package dev.bacecek.launcher.apps

import android.content.ComponentName
import android.os.UserHandle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    onAppClicked: (AppInfo) -> Unit,
    onAppLongClicked: (AppInfo) -> Unit,
    isTitleVisible: Boolean,
    iconCache: AppIconCache = koinInject(),
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .combinedClickable(
                    onLongClick = { onAppLongClicked(appInfo) },
                    onClick = { onAppClicked(appInfo) },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                )
        ) {
            val icon = remember(appInfo.component, appInfo.user) {
                iconCache.getIcon(appInfo.component, appInfo.user)
            }
            Image(
                painter = rememberDrawablePainter(drawable = icon),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop,
            )
            if (isTitleVisible) {
                Spacer(modifier = Modifier.height(8.dp))
                AppName(appInfo.name)
            }
        }
    }
}

@Composable
private fun AppName(name: String) {
    Text(
        text = name,
        textAlign = TextAlign.Center,
        minLines = 1,
        maxLines = 1,
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

@Preview
@Composable
private fun AppIconPreview() {
    AppIcon(
        appInfo = AppInfo(
            name = "App Name",
            packageName = "com.example.app",
            component = ComponentName("com.example.app", "com.example.app.MainActivity"),
            activityClassName = null,
            user = UserHandle.getUserHandleForUid(0),
            isSystemApp = false,
        ),
        onAppClicked = {},
        onAppLongClicked = {},
        isTitleVisible = true,
    )
}