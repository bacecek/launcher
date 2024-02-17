package dev.bacecek.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.UserManager

interface LauncherAppsFacade {
    fun launchApp(appInfo: AppInfo)
    fun uninstall(appInfo: AppInfo)
    fun openAppInfo(appInfo: AppInfo)
    fun loadInstalledApps(): List<AppInfo>
}

internal class LauncherAppsFacadeImpl(
    private val context: Context,
) : LauncherAppsFacade {
    private val userManager: UserManager
        get() = context.requireSystemService()
    private val launcherApps: LauncherApps
        get() = context.requireSystemService()

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

    override fun loadInstalledApps(): List<AppInfo> {
        return userManager.userProfiles
            .asSequence()
            .flatMap { launcherApps.getActivityList(null, it) }
            .map { app ->
                AppInfo(
                    name = app.label.toString(),
                    icon = app.getIcon(0),
                    packageName = app.applicationInfo.packageName,
                    activityClassName = app.componentName.className,
                    component = app.componentName,
                    user = app.user,
                    isSystemApp = context.isSystemApp(app.applicationInfo.packageName)
                )
            }
            .toList()
    }

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

}
