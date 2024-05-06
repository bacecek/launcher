package dev.bacecek.launcher.ui

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

fun Modifier.fadingEdges(
    scrollState: ScrollableState,
    topEdgeHeight: Dp,
    bottomEdgeHeight: Dp,
): Modifier = this.then(
    Modifier
        .graphicsLayer { alpha = 0.99F }
        .drawWithContent {
            drawContent()

            if (scrollState.canScrollBackward) {
                val topColors = listOf(Color.Transparent, Color.Black)
                val topStartY = 0f
                val topGradientHeight = topEdgeHeight.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = topColors,
                        startY = topStartY,
                        endY = topStartY + topGradientHeight
                    ),
                    blendMode = BlendMode.DstIn
                )
            }

            if (scrollState.canScrollForward) {
                val bottomColors = listOf(Color.Black, Color.Transparent)
                val bottomEndY = size.height
                val bottomGradientHeight = bottomEdgeHeight.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = bottomColors,
                        startY = bottomEndY - bottomGradientHeight,
                        endY = bottomEndY
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }
)