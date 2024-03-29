package dev.bacecek.launcher

import android.content.Context
import androidx.core.content.getSystemService

inline fun <reified T : Any> Context.requireSystemService(): T = requireNotNull(getSystemService()) {
    "${T::class} service not found"
}
