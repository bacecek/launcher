package dev.bacecek.launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")
private val GRID_SIZE_KEY = intPreferencesKey("gridSize")

interface SettingsDataStore {
    val gridSize: Flow<Int?>
}

internal class SettingsDataStoreImpl(
    context: Context,
) : SettingsDataStore {

    override val gridSize: Flow<Int?> = context.dataStore.data.map { it[GRID_SIZE_KEY] }

}
