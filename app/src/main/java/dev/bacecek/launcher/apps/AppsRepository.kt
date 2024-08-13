package dev.bacecek.launcher.apps

import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.icu.text.Collator
import android.os.UserManager
import dev.bacecek.launcher.BuildConfig
import dev.bacecek.launcher.di.CoroutineDispatchers
import dev.bacecek.launcher.utils.GlobalLocaleChangeDispatcher
import dev.bacecek.launcher.utils.requireSystemService
import dev.bacecek.launcher.utils.sortedWithCollatorBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

interface AppsRepository {
    val apps: Flow<List<AppInfo>>
    fun update()
}

internal class AppsRepositoryImpl(
    private val context: Context,
    private val dispatchers: CoroutineDispatchers,
    private val coroutineScope: CoroutineScope,
    private val appsEventsDispatcher: AppEventsDispatcher,
    private val localeChangeDispatcher: GlobalLocaleChangeDispatcher,
) : AppsRepository {
    private val userManager: UserManager by lazy { context.requireSystemService() }
    private val launcherApps: LauncherApps by lazy { context.requireSystemService() }

    //TODO(bacecek): explicit backing field
    private val _apps = MutableStateFlow(emptyList<AppInfo>())

    init {
        update()

        coroutineScope.launch(dispatchers.main) {
            appsEventsDispatcher.eventsFlow().collect {
                update()
            }
        }

        coroutineScope.launch(dispatchers.main) {
            localeChangeDispatcher.flow.collect {
                update()
            }
        }
    }

    override val apps: Flow<List<AppInfo>>
        get() = _apps

    override fun update() {
        coroutineScope.launch(dispatchers.io) {
            _apps.value = loadAppList()
        }
    }

    private fun loadAppList(): List<AppInfo> {
        return userManager.userProfiles
            .asSequence()
            .flatMap { launcherApps.getActivityList(null, it) }
            .map { it.toAppInfo(context) }
            .filter { it.packageName != BuildConfig.APPLICATION_ID }
            .filter { !FILTERED_COMPONENTS.contains(it.component) }
            .sortedWithCollatorBy(Collator.getInstance(localeChangeDispatcher.flow.value)) { it.name }
            .toList()
    }

    private fun LauncherActivityInfo.toAppInfo(context: Context) = AppInfo(
        name = label.toString(),
        icon = getIcon(0),
        packageName = applicationInfo.packageName,
        activityClassName = componentName.className,
        component = componentName,
        user = user,
        isSystemApp = applicationInfo.isSystemApp(context),
    )

    private fun ApplicationInfo.isSystemApp(context: Context): Boolean {
        if (packageName.isBlank()) return true
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                    || (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0))
        } catch (e: Exception) {
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
