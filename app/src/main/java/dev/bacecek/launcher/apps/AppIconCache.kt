package dev.bacecek.launcher.apps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.UserHandle
import androidx.collection.LruCache
import dev.bacecek.launcher.utils.requireSystemService

class AppIconCache(
    private val context: Context,
) {
    private val launcherApps: LauncherApps by lazy { context.requireSystemService() }
    private val cache = LruCache<CacheKey, Drawable>(DEFAULT_CACHE_SIZE)

    fun getIcon(component: ComponentName, user: UserHandle): Drawable? {
        val key = CacheKey(component, user)
        return cache[key] ?: loadIcon(key)
    }

    private fun loadIcon(key: CacheKey): Drawable? {
        return try {
            val info = launcherApps.getActivityList(key.component.packageName, key.user)
                .firstOrNull { it.componentName == key.component }
            info?.getIcon(0)?.also { cache.put(key, it) }
        } catch (e: Exception) {
            null
        }
    }

    private data class CacheKey(
        val component: ComponentName,
        val user: UserHandle,
    )

    companion object {
        private const val DEFAULT_CACHE_SIZE = 100
    }
} 