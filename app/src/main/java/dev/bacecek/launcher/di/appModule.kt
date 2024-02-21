package dev.bacecek.launcher.di

import dev.bacecek.launcher.AppListViewModel
import dev.bacecek.launcher.LauncherAppsFacade
import dev.bacecek.launcher.LauncherAppsFacadeImpl
import dev.bacecek.launcher.recent.RecentsDataSource
import dev.bacecek.launcher.recent.RecentsDataSourceImpl
import dev.bacecek.launcher.settings.SettingsDataStore
import dev.bacecek.launcher.settings.SettingsDataStoreImpl
import dev.bacecek.launcher.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<LauncherAppsFacade> { LauncherAppsFacadeImpl(get(), get(), get()) }
    single<RecentsDataSource> { RecentsDataSourceImpl(get()) }
    single<SettingsDataStore> { SettingsDataStoreImpl(get(), get()) }

    single<CoroutineDispatchers> { RealDispatchers() }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    viewModel { AppListViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
