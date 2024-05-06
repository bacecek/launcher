package dev.bacecek.launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.dataStore by preferencesDataStore(name = "settings")

private val GRID_SIZE_KEY = intPreferencesKey("gridSize")
private const val DEFAULT_GRID_SIZE = 4

private val ENABLED_RECENTS_KEY = booleanPreferencesKey("enabledRecents")
private const val DEFAULT_ENABLED_RECENTS = true

interface SettingsRepository {
    val gridSize: StateFlow<Int>
    val isRecentsEnabled: StateFlow<Boolean>

    suspend fun setGridSize(size: Int)
    suspend fun setRecentsEnabled(enabled: Boolean)
}

internal class SettingsRepositoryImpl(
    context: Context,
    coroutineScope: CoroutineScope,
) : SettingsRepository {
    private val dataStore = context.dataStore

    override val gridSize: StateFlow<Int> = dataStore.data.map {
        it[GRID_SIZE_KEY] ?: DEFAULT_GRID_SIZE
    }.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = DEFAULT_GRID_SIZE)

    override val isRecentsEnabled: StateFlow<Boolean> = dataStore.data.map {
        it[ENABLED_RECENTS_KEY] ?: DEFAULT_ENABLED_RECENTS
    }.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = DEFAULT_ENABLED_RECENTS)

    override suspend fun setGridSize(size: Int) {
        dataStore.edit { settings ->
            settings[GRID_SIZE_KEY] = size
        }
    }

    override suspend fun setRecentsEnabled(enabled: Boolean) {
        dataStore.edit { settings ->
            settings[ENABLED_RECENTS_KEY] = enabled
        }
    }
}
