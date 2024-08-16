package dev.bacecek.launcher.utils

import android.content.res.Configuration
import androidx.core.os.ConfigurationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class GlobalLocaleChangeDispatcher {
    val flow: StateFlow<Locale>
        field = MutableStateFlow(Locale.getDefault())

    fun dispatch(newConfig: Configuration) {
        val primaryLocale = ConfigurationCompat.getLocales(newConfig)[0] ?: Locale.getDefault()
        flow.value = primaryLocale
    }
}
