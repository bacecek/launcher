package dev.bacecek.launcher.di

import android.content.Context
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class DiVerificationTest : KoinTest {

    @Test
    fun checkAllModules() {
        appModule.verify(
            extraTypes = listOf(Context::class),
        )
    }

}
