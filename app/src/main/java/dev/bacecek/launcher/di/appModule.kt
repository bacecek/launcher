package dev.bacecek.launcher.di

import dev.bacecek.launcher.AppListViewModel
import dev.bacecek.launcher.recent.RecentsDataSource
import dev.bacecek.launcher.recent.RecentsDataSourceImpl
import dev.bacecek.launcher.settings.SettingsDataStore
import dev.bacecek.launcher.settings.SettingsDataStoreImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<RecentsDataSource> { RecentsDataSourceImpl(get()) }
    single<SettingsDataStore> { SettingsDataStoreImpl(get()) }
    viewModel { AppListViewModel(get(), get(), get()) }
}
