package dev.bacecek.launcher.ui

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Applies overscroll effect only on Android S+ where [android.widget.EdgeEffect] is available.
 * Ignores glow effect that is used on Android R and lower.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BounceEdgeEffect(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            OverscrollConfiguration()
        } else null,
        content = content,
    )
}
