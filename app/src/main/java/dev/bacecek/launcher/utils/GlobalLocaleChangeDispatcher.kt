package dev.bacecek.launcher.utils

import android.content.res.Configuration
import androidx.core.os.ConfigurationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class GlobalLocaleChangeDispatcher {
    private val _flow = MutableStateFlow(Locale.getDefault())
    val flow: StateFlow<Locale>
        get() = _flow

    fun dispatch(newConfig: Configuration) {
        val primaryLocale = ConfigurationCompat.getLocales(newConfig)[0] ?: Locale.getDefault()
        _flow.value = primaryLocale
    }
}
