package dev.bacecek.launcher

import android.app.Application
import android.content.res.Configuration
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import dev.bacecek.launcher.di.appModule
import dev.bacecek.launcher.utils.GlobalLocaleChangeDispatcher
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LauncherApplication : Application() {
    private val configurationChangeDispatcher: GlobalLocaleChangeDispatcher by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LauncherApplication)
            modules(appModule)
        }

        configurationChangeDispatcher.dispatch(resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChangeDispatcher.dispatch(newConfig)
    }

}
