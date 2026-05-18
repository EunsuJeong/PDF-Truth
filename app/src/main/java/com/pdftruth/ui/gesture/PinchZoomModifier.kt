@file:Suppress("unused")

package com.pdftruth.ui.gesture

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Pinch Zoom 및 scale 상태를 관리하는 Modifier 확장
 */
fun Modifier.pinchToZoom(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    minScale: Float = 1f,
    maxScale: Float = 5f
): Modifier = pointerInput(Unit) {
    detectTransformGestures { _, _, zoom, _ ->
        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
        onScaleChange(newScale)
    }
}