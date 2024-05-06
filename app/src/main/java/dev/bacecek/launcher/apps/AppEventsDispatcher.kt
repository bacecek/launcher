package dev.bacecek.launcher.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import dev.bacecek.launcher.utils.requireSystemService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

class AppEventsDispatcher(
    private val context: Context,
) {
    fun eventsFlow(): Flow<AppEvent> = callbackFlow {
        val launcherApps: LauncherApps = context.requireSystemService()
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                trySend(AppEvent.Removed(packageName, user))
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                trySend(AppEvent.Added(packageName, user))
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                trySend(AppEvent.Changed(packageName, user))
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(AppEvent.AppsAvailable(packageNames.toList(), user, replacing))
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(AppEvent.AppsUnavailable(packageNames.toList(), user, replacing))
            }
        }
        launcherApps.registerCallback(callback)
        awaitClose { launcherApps.unregisterCallback(callback) }
    }.buffer(Channel.RENDEZVOUS)
}

sealed interface AppEvent {
    data class Added(val packageName: String, val user: UserHandle) : AppEvent
    data class Changed(val packageName: String, val user: UserHandle) : AppEvent
    data class Removed(val packageName: String, val user: UserHandle) : AppEvent
    data class AppsAvailable(val packageNames: List<String>, val user: UserHandle, val replacing: Boolean) : AppEvent
    data class AppsUnavailable(val packageNames: List<String>, val user: UserHandle, val replacing: Boolean) : AppEvent
}
