package dev.bacecek.launcher.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bacecek.launcher.di.CoroutineDispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ViewModel() {

    val gridSize: StateFlow<Int>
        get() = settingsDataStore.gridSize

    val availableGridSizes: List<Int> = listOf(3, 4, 5, 6)

    fun setGridSize(size: Int) {
        viewModelScope.launch(coroutineDispatchers.io) {
            settingsDataStore.setGridSize(size)
        }
    }

}
