package dev.bacecek.launcher

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recent")

class RecentUsedDataSource(
    private val context: Context
) {

    val recentUsedApps: Flow<Map<String, Long>> = context.dataStore
        .data
        .map {  preferences ->
            preferences.asMap()
                .mapKeys { it.key.name }
                .mapValues { it.value as Long }
        }

    suspend fun onAppUsed(appInfo: AppInfo) {
        context.dataStore.edit { preferences ->
            val key = longPreferencesKey(appInfo.packageName)
            preferences[key] = System.currentTimeMillis()
        }
    }

}
