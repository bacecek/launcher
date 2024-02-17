package dev.bacecek.launcher

import android.app.Application
import dev.bacecek.launcher.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LauncherApplication)
            modules(appModule)
        }
    }

}
