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
    onScaleChange: (Float, Offset) -> Unit,
    onPanChange: (Offset) -> Unit,
    onTransformEnd: () -> Unit,
): Modifier = composed {
    val context = LocalContext.current
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var isTransforming by remember { mutableStateOf(false) }
    var suppressNextTap by remember { mutableStateOf(false) }
    var pinchCenter by remember { mutableStateOf(Offset.Zero) }

    fun extractPinchCenter(event: MotionEvent): Offset {
        return if (event.pointerCount >= 2) {
            val centerX = (event.getX(0) + event.getX(1)) / 2f
            val centerY = (event.getY(0) + event.getY(1)) / 2f
            Offset(centerX, centerY)
        } else {
            Offset(event.x, event.y)
        }
    }

    val scaleDetector = remember {
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    if (!isTransforming) {
                        isTransforming = true
                        onTransformStart()
                    }
                    suppressNextTap = true
                    return true
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor
                    val focus = if (pinchCenter == Offset.Zero) {
                        Offset(detector.focusX, detector.focusY)
                    } else {
                        pinchCenter
                    }
                    onScaleChange(scaleFactor, focus)
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
                    if (!isTransforming && !suppressNextTap) {
                        onSingleTap(Offset(e.x, e.y), viewSize)
                    } else {
                        suppressNextTap = false
                    }
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (!isTransforming && !suppressNextTap) {
                        onDoubleTap(Offset(e.x, e.y))
                    } else {
                        suppressNextTap = false
                    }
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float,
                ): Boolean {
                    if (e2.pointerCount >= 2 || isTransforming) {
                        suppressNextTap = true
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
            if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                suppressNextTap = true
            }

            if (event.pointerCount >= 2) {
                pinchCenter = extractPinchCenter(event)
            }

            val handledScale = scaleDetector.onTouchEvent(event)
            val handledGesture = gestureDetector.onTouchEvent(event)

            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                if (isTransforming) {
                    isTransforming = false
                    onTransformEnd()
                }
                pinchCenter = Offset.Zero
            }

            handledScale || handledGesture || event.actionMasked == MotionEvent.ACTION_DOWN
        }
}