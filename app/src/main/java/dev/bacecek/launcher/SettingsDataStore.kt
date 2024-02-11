package dev.bacecek.launcher

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")
private val GRID_SIZE_KEY = intPreferencesKey("gridSize")

class SettingsDataStore(context: Context) {

    val gridSize: Flow<Int?> = context.dataStore.data.map { it[GRID_SIZE_KEY] }

}
