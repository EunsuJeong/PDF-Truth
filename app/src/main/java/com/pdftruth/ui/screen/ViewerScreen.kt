package com.pdftruth.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import com.pdftruth.ui.gesture.pinchToZoom
import com.pdftruth.viewmodel.ViewerUiState
import com.pdftruth.viewmodel.PageUiState

@Composable
fun ViewerScreen(
    uiState: ViewerUiState,
    onNavigateUp: () -> Unit,
    onCurrentPageChanged: (Int) -> Unit = {},
    onToggleBookmark: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchExecute: () -> Unit = {},
    onSearchResultClick: (Int) -> Unit = {},
    onToggleThumbnails: () -> Unit = {},
    onThumbnailClick: (Int) -> Unit = {},
    pdfUri: Uri? = null,
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1A1A1A))
        ) {
            when (uiState) {
                is ViewerUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("PDF 파일을 선택하세요.", color = Color.White)
                        Button(
                            onClick = onNavigateUp,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("뒤로")
                        }
                    }
                }
                is ViewerUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                is ViewerUiState.Success -> {
                    val pageCount = uiState.pageCount
                    val currentPage = uiState.currentPage
                    var scale by remember(currentPage) { mutableFloatStateOf(1f) }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 상단 바
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
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text("뒤로", color = Color.White, fontSize = 12.sp)
                            }
                            Text(
                                text = (uiState.fileName ?: "PDF").take(30),
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // PDF 페이지 표시 영역 - 좌우 50% 터치 네비게이션
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                uiState.pages.getOrNull(currentPage) is PageUiState.BitmapReady -> {
                                    val bitmap = (uiState.pages[currentPage] as PageUiState.BitmapReady).bitmap
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "PDF 페이지 ${currentPage + 1}",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize(0.95f)
                                            .scale(scale)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = { offset ->
                                                        val tapX = offset.x
                                                        val width = size.width
                                                        if (tapX < width / 2) {
                                                            if (currentPage > 0) {
                                                                onCurrentPageChanged(currentPage - 1)
                                                            }
                                                        } else {
                                                            if (currentPage < pageCount - 1) {
                                                                onCurrentPageChanged(currentPage + 1)
                                                            }
                                                        }
                                                    },
                                                    onDoubleTap = {
                                                        scale = if (scale <= 1f) 2f else 1f
                                                    }
                                                )
                                            }
                                            .pinchToZoom(
                                                scale = scale,
                                                onScaleChange = { scale = it },
                                                minScale = 1f,
                                                maxScale = 5f
                                            )
                                    )
                                }
                                uiState.pages.getOrNull(currentPage) is PageUiState.Loading -> {
                                    CircularProgressIndicator(color = Color.White)
                                }
                                else -> {
                                    Text(
                                        "페이지를 불러오는 중...",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // 하단 페이지 표시
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${currentPage + 1} / $pageCount",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .background(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                is ViewerUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "오류: ${uiState.message}",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = onNavigateUp,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("뒤로")
                        }
                    }
                }
            }
        }
    }
}