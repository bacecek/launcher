package dev.bacecek.launcher.di

import android.app.Application
import dev.zacsweers.metro.createGraphFactory

object DI {
    lateinit var graph: AppGraph
        private set

    fun initialize(application: Application) {
        graph = createGraphFactory<AppGraph.Factory>().create(application)
    }
}
