package dev.bacecek.launcher.di

import android.app.Application
import com.slack.circuit.foundation.Circuit
import dev.bacecek.launcher.apps.AppEventsDispatcher
import dev.bacecek.launcher.apps.AppIconCache
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
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph
interface AppGraph {
    val circuit: Circuit
    val configurationDispatcher: GlobalLocaleChangeDispatcher
    val appIconCache: AppIconCache

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }

    @Provides
    fun provideCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    fun provideDispatchers(): CoroutineDispatchers = RealDispatchers()

    @Provides
    fun provideAppEventsDispatcher(application: Application): AppEventsDispatcher =
        AppEventsDispatcher(application)

    @Provides
    fun provideGlobalLocaleChangeDispatcher(): GlobalLocaleChangeDispatcher =
        GlobalLocaleChangeDispatcher()

    @Provides
    fun provideAppIconCache(application: Application): AppIconCache = AppIconCache(application)

    @Provides
    fun provideAppsRepository(
        application: Application,
        dispatchers: CoroutineDispatchers,
        scope: CoroutineScope,
        events: AppEventsDispatcher,
        localeChangeDispatcher: GlobalLocaleChangeDispatcher,
        iconCache: AppIconCache,
    ): AppsRepository =
        AppsRepositoryImpl(application, dispatchers, scope, events, localeChangeDispatcher, iconCache)

    @Provides
    fun provideRecentsRepository(
        application: Application,
        events: AppEventsDispatcher,
        dispatchers: CoroutineDispatchers,
        scope: CoroutineScope,
    ): RecentsRepository =
        RecentsRepositoryImpl(application, events, dispatchers, scope)

    @Provides
    fun provideSettingsRepository(
        application: Application,
        scope: CoroutineScope,
    ): SettingsRepository =
        SettingsRepositoryImpl(application, scope)

    @Provides
    fun provideDefaultLauncherRepository(
        scope: CoroutineScope,
        dispatchers: CoroutineDispatchers,
        application: Application,
    ): DefaultLauncherRepository =
        DefaultLauncherRepositoryImpl(scope, dispatchers, application)

    @Provides
    fun provideAppsPresenterFactory(
        repository: AppsRepository,
        recentsRepository: RecentsRepository,
        settingsRepository: SettingsRepository,
        dispatchers: CoroutineDispatchers,
    ): AppsPresenter.Factory =
        AppsPresenter.Factory(repository, recentsRepository, settingsRepository, dispatchers)

    @Provides
    fun provideSettingsPresenterFactory(
        repository: SettingsRepository,
        defaultLauncherRepository: DefaultLauncherRepository,
    ): SettingsPresenter.Factory =
        SettingsPresenter.Factory(repository, defaultLauncherRepository)

    @Provides
    fun provideCircuit(
        appsPresenterFactory: AppsPresenter.Factory,
        settingsPresenterFactory: SettingsPresenter.Factory,
    ): Circuit = Circuit.Builder()
        .addPresenterFactory(appsPresenterFactory, settingsPresenterFactory)
        .addUi<AppsScreen, AppsScreen.State> { state, modifier -> AppsScreen(state, modifier) }
        .addUi<SettingsScreen, SettingsScreen.State> { state, modifier -> SettingsScreen(state, modifier) }
        .build()

}
