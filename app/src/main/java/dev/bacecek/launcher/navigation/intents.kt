@file:Suppress("FunctionName")

package dev.bacecek.launcher.navigation

import android.content.Intent
import androidx.core.net.toUri
import com.slack.circuitx.android.IntentScreen

fun UninstallAppScreen(packageName: String) = IntentScreen(
    intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
)

fun WallpaperPickerScreen() = IntentScreen(
    intent = Intent(Intent.ACTION_SET_WALLPAPER)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
)
