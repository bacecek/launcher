package dev.bacecek.launcher.apps

import android.content.ComponentName
import androidx.core.os.UserHandleCompat
import android.graphics.drawable.Drawable

@JvmInline
value class UserHandleUid(val value: Int)

fun UserHandleUid.toUserHandle() = UserHandleCompat.getUserHandleForUid(value)

data class AppInfo(
    val name: String,
    val component: ComponentName,
    val user: UserHandleUid,
    val isSystemApp: Boolean,
    val icon: Drawable?,
)
