package dev.bacecek.launcher.ui

import android.os.Build
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.rememberPlatformOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Applies overscroll effect only on Android S+ where [android.widget.EdgeEffect] is available.
 * Ignores glow effect that is used on Android R and lower.
 */
@Composable
fun BounceEdgeEffect(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalOverscrollFactory provides if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rememberPlatformOverscrollFactory()
        } else null,
        content = content,
    )
}
