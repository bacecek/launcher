package dev.bacecek.launcher

import android.app.Application
import android.content.res.Configuration
import dev.bacecek.launcher.di.DI
import dev.bacecek.launcher.utils.GlobalLocaleChangeDispatcher

class LauncherApplication : Application() {
    private val configurationChangeDispatcher: GlobalLocaleChangeDispatcher
        get() = DI.graph.configurationDispatcher

    override fun onCreate() {
        super.onCreate()

        DI.initialize(this)

        configurationChangeDispatcher.dispatch(resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChangeDispatcher.dispatch(newConfig)
    }

}
