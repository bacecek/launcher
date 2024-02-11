package dev.bacecek.launcher

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.UserHandle
import androidx.compose.runtime.Stable

@Stable
data class AppInfo(
    val name: String,
    val icon: Drawable?,
    val packageName: String,
    val activityClassName: String?,
    val component: ComponentName,
    val user: UserHandle,
    val isSystemApp: Boolean,
)
