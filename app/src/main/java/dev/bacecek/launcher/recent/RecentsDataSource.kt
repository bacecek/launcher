package dev.bacecek.launcher.recent

import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recent")

typealias LastUsedTimestamp = Long

interface RecentsDataSource {
    val recentUsedApps: Flow<Map<ComponentName, LastUsedTimestamp>>

    suspend fun onAppUsed(componentName: ComponentName)
}

internal class RecentsDataSourceImpl(
    private val context: Context
) : RecentsDataSource {

    override val recentUsedApps: Flow<Map<ComponentName, LastUsedTimestamp>> = context.dataStore
        .data
        .map { preferences ->
            preferences.asMap()
                .mapNotNull {
                    val componentName = ComponentName.unflattenFromString(it.key.name) ?: return@mapNotNull null
                    componentName to it.value as LastUsedTimestamp
                }
                .toMap()
        }

    override suspend fun onAppUsed(componentName: ComponentName) {
        context.dataStore.edit { preferences ->
            val key = longPreferencesKey(componentName.flattenToString())
            preferences[key] = System.currentTimeMillis()
        }
    }

}
