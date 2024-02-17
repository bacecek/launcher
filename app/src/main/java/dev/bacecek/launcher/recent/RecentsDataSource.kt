package dev.bacecek.launcher.recent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recent")

typealias RecentPackageName = String
typealias LastUsedTimestamp = Long

interface RecentsDataSource {
    val recentUsedApps: Flow<Map<RecentPackageName, LastUsedTimestamp>>

    suspend fun onAppUsed(packageName: RecentPackageName)
}

internal class RecentsDataSourceImpl(
    private val context: Context
) : RecentsDataSource {

    override val recentUsedApps: Flow<Map<RecentPackageName, LastUsedTimestamp>> = context.dataStore
        .data
        .map { preferences ->
            preferences.asMap()
                .mapKeys { it.key.name }
                .mapValues { it.value as LastUsedTimestamp }
        }

    override suspend fun onAppUsed(packageName: RecentPackageName) {
        context.dataStore.edit { preferences ->
            val key = longPreferencesKey(packageName)
            preferences[key] = System.currentTimeMillis()
        }
    }

}
