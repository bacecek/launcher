package dev.bacecek.launcher.di

import com.slack.circuit.foundation.Circuit
import dev.bacecek.launcher.apps.AppEventsDispatcher
import dev.bacecek.launcher.apps.AppsPresenter
import dev.bacecek.launcher.apps.AppsRepository
import dev.bacecek.launcher.apps.AppsRepositoryImpl
import dev.bacecek.launcher.apps.AppsScreen
import dev.bacecek.launcher.recent.RecentsRepository
import dev.bacecek.launcher.recent.RecentsRepositoryImpl
import dev.bacecek.launcher.make_default.DefaultLauncherRepository
import dev.bacecek.launcher.make_default.DefaultLauncherRepositoryImpl
import dev.bacecek.launcher.settings.SettingsPresenter
import dev.bacecek.launcher.settings.SettingsRepository
import dev.bacecek.launcher.settings.SettingsRepositoryImpl
import dev.bacecek.launcher.settings.SettingsScreen
import dev.bacecek.launcher.utils.GlobalLocaleChangeDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val appModule = module {
    single<AppsRepository> { AppsRepositoryImpl(get(), get(), get(), get(), get()) }
    single<RecentsRepository> { RecentsRepositoryImpl(get(), get(), get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get()) }
    single<AppEventsDispatcher> { AppEventsDispatcher(get()) }
    single<GlobalLocaleChangeDispatcher> { GlobalLocaleChangeDispatcher() }

    single<CoroutineDispatchers> { RealDispatchers() }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<DefaultLauncherRepository> { DefaultLauncherRepositoryImpl(get(), get(), get()) }

    single<AppsPresenter.Factory> { AppsPresenter.Factory(get(), get(), get(), get()) }
    single<SettingsPresenter.Factory> { SettingsPresenter.Factory(get(), get()) }
    single<Circuit> { Circuit.Builder()
        .addPresenterFactory(
            get<AppsPresenter.Factory>(),
            get<SettingsPresenter.Factory>()
        )
        .addUi<AppsScreen, AppsScreen.State> { state, modifier -> AppsScreen(state, modifier) }
        .addUi<SettingsScreen, SettingsScreen.State> { state, modifier -> SettingsScreen(state, modifier) }
        .build()
    }
}
