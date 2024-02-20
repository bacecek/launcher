package dev.bacecek.launcher.di

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineDispatchers {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
}

internal class RealDispatchers : CoroutineDispatchers {
    override val io: CoroutineDispatcher
        get() = kotlinx.coroutines.Dispatchers.IO
    override val main: CoroutineDispatcher
        get() = kotlinx.coroutines.Dispatchers.Main
}
