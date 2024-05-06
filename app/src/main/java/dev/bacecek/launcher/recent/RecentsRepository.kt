package dev.bacecek.launcher.recent

import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.bacecek.launcher.apps.AppEvent
import dev.bacecek.launcher.apps.AppEventsDispatcher
import dev.bacecek.launcher.di.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recent")

interface RecentsRepository {
    val recentUsedApps: Flow<Map<ComponentName, Long>>

    suspend fun markAppUsed(componentName: ComponentName)
    suspend fun remove(componentName: ComponentName)
}

internal class RecentsRepositoryImpl(
    private val context: Context,
    private val appsEventsDispatcher: AppEventsDispatcher,
    private val dispatchers: CoroutineDispatchers,
    coroutineScope: CoroutineScope,
) : RecentsRepository {

    init {
        coroutineScope.launch(dispatchers.main) {
            appsEventsDispatcher.eventsFlow().collect { event ->
                when (event) {
                    is AppEvent.Removed -> handleRemovedApps(listOf(event.packageName))
                    is AppEvent.AppsUnavailable -> handleRemovedApps(event.packageNames)
                    else -> Unit
                }
            }
        }
    }

    override val recentUsedApps: Flow<Map<ComponentName, Long>> = context.dataStore
        .data
        .map { preferences ->
            preferences.asMap()
                .mapNotNull { entry ->
                    ComponentName.unflattenFromString(entry.key.name)?.let { componentName ->
                        componentName to entry.value as Long
                    }
                }
                .toMap()
        }

    override suspend fun markAppUsed(componentName: ComponentName) {
        context.dataStore.edit { preferences ->
            val key = longPreferencesKey(componentName.flattenToString())
            preferences[key] = System.currentTimeMillis()
        }
    }

    override suspend fun remove(componentName: ComponentName) {
        context.dataStore.edit { preferences ->
            preferences.remove(longPreferencesKey(componentName.flattenToString()))
        }
    }

    private suspend fun handleRemovedApps(packageNames: List<String>) = withContext(dispatchers.io) {
        context.dataStore.updateData { preferences ->
            val toRemove = preferences.asMap().filter { entry ->
                packageNames.contains(ComponentName.unflattenFromString(entry.key.name)?.packageName)
            }
            preferences.toMutablePreferences().apply {
                toRemove.forEach { (key, _) ->
                    remove(key)
                }
            }
        }
    }
}
