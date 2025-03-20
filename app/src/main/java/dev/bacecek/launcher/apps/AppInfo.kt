package dev.bacecek.launcher.apps

import android.content.ComponentName
import android.os.UserHandle

data class AppInfo(
    val name: String,
    val packageName: String,
    val activityClassName: String?,
    val component: ComponentName,
    val user: UserHandle,
    val isSystemApp: Boolean,
)
