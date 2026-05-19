package com.pdftruth.ui.gesture

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.pdfPageGestureModifier(
    onSingleTap: (Offset, IntSize) -> Unit,
    onDoubleTap: (Offset) -> Unit,
    onTransformStart: () -> Unit,
    onScaleChange: (Float) -> Unit,
    onPanChange: (Offset) -> Unit,
    onTransformEnd: () -> Unit,
): Modifier = composed {
    val context = LocalContext.current
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var isTransforming by remember { mutableStateOf(false) }

    val scaleDetector = remember {
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    if (!isTransforming) {
                        isTransforming = true
                        onTransformStart()
                    }
                    return true
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    onScaleChange(detector.scaleFactor)
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    if (isTransforming) {
                        isTransforming = false
                        onTransformEnd()
                    }
                }
            },
        )
    }

    val gestureDetector = remember {
        GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    onSingleTap(Offset(e.x, e.y), viewSize)
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    onDoubleTap(Offset(e.x, e.y))
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float,
                ): Boolean {
                    if (e2.pointerCount >= 2 || isTransforming) {
                        return false
                    }
                    onPanChange(Offset(-distanceX, -distanceY))
                    return true
                }
            },
        )
    }

    this
        .onSizeChanged { size -> viewSize = size }
        .pointerInteropFilter { event ->
            val handledScale = scaleDetector.onTouchEvent(event)
            val handledGesture = gestureDetector.onTouchEvent(event)

            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                if (isTransforming) {
                    isTransforming = false
                    onTransformEnd()
                }
            }

            handledScale || handledGesture
        }
}