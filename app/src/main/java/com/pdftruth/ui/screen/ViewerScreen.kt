package com.pdftruth.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdftruth.ui.gesture.pdfPageGestureModifier
import com.pdftruth.viewmodel.PageUiState
import com.pdftruth.viewmodel.ViewerUiState
import kotlin.math.abs

@Composable
fun ViewerScreen(
    uiState: ViewerUiState,
    onNavigateUp: () -> Unit,
    onCurrentPageChanged: (Int) -> Unit = {},
    onPreviousPage: () -> Unit = {},
    onNextPage: () -> Unit = {},
    onToggleBookmark: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchExecute: () -> Unit = {},
    onSearchResultClick: (Int) -> Unit = {},
    onToggleThumbnails: () -> Unit = {},
    onThumbnailClick: (Int) -> Unit = {},
    pdfUri: Uri? = null,
) {
    val darkBackground = Color(0xFF1A1A1A)

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(darkBackground)
        ) {
            when (uiState) {
                is ViewerUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                        , verticalArrangement = Arrangement.Center
                    ) {
                        Text("PDF 파일을 선택하세요.", color = Color.White)
                        Button(onClick = onNavigateUp, modifier = Modifier.padding(top = 16.dp)) {
                            Text("뒤로")
                        }
                    }
                }
                is ViewerUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                    )
                }
                is ViewerUiState.Success -> {
                    val pageCount = uiState.pageCount
                    val currentPage = uiState.currentPage
                    val currentPageState = uiState.pages.getOrNull(currentPage)

                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    var isTransforming by remember { mutableStateOf(false) }
                    var contentSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

                    fun resetTransform() {
                        scale = 1f
                        offset = Offset.Zero
                        isTransforming = false
                    }

                    fun clampOffset(raw: Offset, targetScale: Float): Offset {
                        if (targetScale <= 1f || contentSize == androidx.compose.ui.unit.IntSize.Zero) {
                            return Offset.Zero
                        }
                        val maxX = (contentSize.width * (targetScale - 1f) / 2f).coerceAtLeast(0f)
                        val maxY = (contentSize.height * (targetScale - 1f) / 2f).coerceAtLeast(0f)
                        val clampedX = raw.x.coerceIn(-maxX, maxX)
                        val clampedY = raw.y.coerceIn(-maxY, maxY)
                        return Offset(clampedX, clampedY)
                    }

                    LaunchedEffect(currentPage, scale) {
                        println("[DEBUG] LaunchedEffect triggered: currentPage=$currentPage, scale=$scale")
                        resetTransform()
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onNavigateUp,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                modifier = Modifier.padding(4.dp),
                            ) {
                                Text("뒤로", color = Color.White, fontSize = 12.sp)
                            }
                            Text(
                                text = (uiState.fileName ?: "PDF").take(22),
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(darkBackground),
                            contentAlignment = Alignment.Center,
                        ) {
                            when (currentPageState) {
                                is PageUiState.BitmapReady -> {
                                    Image(
                                        bitmap = currentPageState.bitmap.asImageBitmap(),
                                        contentDescription = "PDF 페이지 ${currentPage + 1}",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize(0.96f)
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                                translationX = offset.x
                                                translationY = offset.y
                                            }
                                            .pdfPageGestureModifier(
                                                onSingleTap = { tapOffset, size ->
                                                    if (scale == 1f && !isTransforming) {
                                                        if (tapOffset.x < size.width / 2f) {
                                                            if (currentPage > 0) {
                                                                onPreviousPage()
                                                                onCurrentPageChanged(currentPage - 1)
                                                                resetTransform()
                                                            }
                                                        } else {
                                                            if (currentPage < pageCount - 1) {
                                                                onNextPage()
                                                                onCurrentPageChanged(currentPage + 1)
                                                                resetTransform()
                                                            }
                                                        }
                                                    }
                                                },
                                                onDoubleTap = {
                                                    val nextScale = if (abs(scale - 1f) < 0.01f) 2f else 1f
                                                    scale = nextScale
                                                    if (nextScale == 1f) {
                                                        offset = Offset.Zero
                                                    }
                                                },
                                                onTransformStart = {
                                                    isTransforming = true
                                                },
                                                onScaleChange = { zoomDelta ->
                                                    val targetScale = (scale * zoomDelta).coerceIn(1f, 5f)
                                                    scale = targetScale
                                                    offset = clampOffset(offset, targetScale)
                                                },
                                                onPanChange = { panDelta ->
                                                    if (scale > 1f) {
                                                        offset = clampOffset(offset + panDelta, scale)
                                                    }
                                                },
                                                onTransformEnd = {
                                                    isTransforming = false
                                                    if (scale <= 1f) {
                                                        resetTransform()
                                                    }
                                                },
                                            )
                                    )
                                }

                                is PageUiState.Loading -> {
                                    CircularProgressIndicator(color = Color.White)
                                }

                                is PageUiState.Error -> {
                                    Text(
                                        text = "페이지 ${currentPage + 1} 오류: ${currentPageState.message}",
                                        color = Color(0xFFFFA0A0),
                                    )
                                }

                                null -> {
                                    Text("페이지를 불러오는 중...", color = Color.White)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${currentPage + 1} / $pageCount",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .background(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
                is ViewerUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                        , verticalArrangement = Arrangement.Center
                    ) {
                        Text("오류: ${uiState.message}", color = Color(0xFFFFA0A0))
                        Button(onClick = onNavigateUp, modifier = Modifier.padding(top = 16.dp)) {
                            Text("뒤로")
                        }
                    }
                }
            }
        }
    }
}