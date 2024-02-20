package dev.bacecek.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.UserHandle
import android.os.UserManager
import dev.bacecek.launcher.di.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class AppInfo(
    val name: String,
    val icon: Drawable?,
    val packageName: String,
    val activityClassName: String?,
    val component: ComponentName,
    val user: UserHandle,
    val isSystemApp: Boolean,
)

interface LauncherAppsFacade {
    val apps: Flow<List<AppInfo>>
    val onAppRemoved: Channel<AppInfo>
    fun launchApp(appInfo: AppInfo)
    fun uninstall(appInfo: AppInfo)
    fun openAppInfo(appInfo: AppInfo)
    fun openWallpaperPicker()
}

internal class LauncherAppsFacadeImpl(
    private val context: Context,
    private val dispatchers: CoroutineDispatchers,
    private val coroutineScope: CoroutineScope,
) : LauncherAppsFacade {
    private val userManager: UserManager
        get() = context.requireSystemService()
    private val launcherApps: LauncherApps
        get() = context.requireSystemService()

    init {
        launcherApps.registerCallback(object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                val toRemove = _apps.value.filter { it.packageName == packageName}
                _apps.value = _apps.value.minus(toRemove.toSet())
                coroutineScope.launch(dispatchers.io) {
                    toRemove.forEach { onAppRemoved.send(it) }
                }
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                val newData = launcherApps.getActivityList(packageName, user).map { it.toAppInfo(context) }
                _apps.value = _apps.value.plus(newData)
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                val newData = launcherApps.getActivityList(packageName, user).map { it.toAppInfo(context) }
                _apps.value = _apps.value.filter { it.packageName != packageName }
                    .plus(newData)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean
            ) {
                packageNames.forEach {
                    if (replacing) {
                        onPackageChanged(it, user)
                    } else {
                        onPackageAdded(it, user)
                    }
                }
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean
            ) {
                packageNames.forEach {
                    if (replacing) {
                        onPackageChanged(it, user)
                    } else {
                        onPackageRemoved(it, user)
                    }
                }
            }

        })
    }

    private val _apps = MutableStateFlow(loadAppList())
    override val apps: Flow<List<AppInfo>> = _apps

    override val onAppRemoved: Channel<AppInfo> = Channel(Channel.RENDEZVOUS)

    private fun loadAppList(): List<AppInfo> {
        return userManager.userProfiles
            .asSequence()
            .flatMap { launcherApps.getActivityList(null, it) }
            .map { it.toAppInfo(context)}
            .filter { it.packageName != BuildConfig.APPLICATION_ID }
            .filter { !FILTERED_COMPONENTS.contains(it.component) }
            .toList()
    }

    override fun launchApp(appInfo: AppInfo) {
        val component = if (appInfo.activityClassName.isNullOrBlank()) {
            val activities = launcherApps.getActivityList(appInfo.packageName, appInfo.user)
            activities.lastOrNull()?.let { ComponentName(appInfo.packageName, it.name) }
        } else {
            ComponentName(appInfo.packageName, appInfo.activityClassName)
        }

        component?.let {
            launcherApps.startMainActivity(it, appInfo.user, null, null)
        }
    }

    override fun uninstall(appInfo: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${appInfo.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun openAppInfo(appInfo: AppInfo) {
        launcherApps.startAppDetailsActivity(appInfo.component, appInfo.user, null, null)
    }

    override fun openWallpaperPicker() {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun LauncherActivityInfo.toAppInfo(context: Context) = AppInfo(
        name = label.toString(),
        icon = getIcon(0),
        packageName = applicationInfo.packageName,
        activityClassName = componentName.className,
        component = componentName,
        user = user,
        isSystemApp = context.isSystemApp(applicationInfo.packageName)
    )

    private fun Context.isSystemApp(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                    || (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0))
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        private val FILTERED_COMPONENTS = listOf(
            "com.google.android.googlequicksearchbox/.VoiceSearchActivity",
            "com.google.android.launcher/.StubApp",
            "com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity",
        ).map { ComponentName.unflattenFromString(it) }.toSet()
    }

}
