package dev.bacecek.launcher.settings

import android.content.Context
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

interface SettingsDataStore {
    val gridSize: StateFlow<Int>

    suspend fun setGridSize(size: Int)
}

internal class SettingsDataStoreImpl(
    context: Context,
    coroutineScope: CoroutineScope,
) : SettingsDataStore {
    private val dataStore = context.dataStore

    override val gridSize: StateFlow<Int> = dataStore.data.map {
        it[GRID_SIZE_KEY] ?: DEFAULT_GRID_SIZE
    }.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = DEFAULT_GRID_SIZE)

    override suspend fun setGridSize(size: Int) {
        dataStore.edit { settings ->
            settings[GRID_SIZE_KEY] = size
        }
    }
}
