package dev.bacecek.launcher.utils

import android.content.Context
import android.icu.text.Collator
import androidx.core.content.getSystemService

inline fun <reified T : Any> Context.requireSystemService(): T = requireNotNull(getSystemService()) {
    "${T::class} service not found"
}

inline fun <T> Sequence<T>.sortedWithCollatorBy(
    collator: Collator,
    crossinline selector: (T) -> String?,
): Sequence<T> {
    return sortedWith { a, b -> collator.compare(selector(a), selector(b)) }
}
