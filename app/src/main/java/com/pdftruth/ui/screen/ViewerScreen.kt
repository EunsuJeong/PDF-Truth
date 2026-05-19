package com.pdftruth.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.net.Uri
import com.pdftruth.ui.gesture.pinchToZoom
import com.pdftruth.viewmodel.ViewerUiState

@Composable
fun ViewerScreen(
    uiState: ViewerUiState,
    onNavigateUp: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    pdfUri: Uri? = null,
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is ViewerUiState.Idle -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("PDF 파일을 선택하세요.")
                        Button(onClick = onNavigateUp, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Back")
                        }
                    }
                }
                is ViewerUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ViewerUiState.Success -> {
                    val currentPageBitmap = uiState.currentPageBitmap
                    val currentPage = uiState.currentPage
                    val totalPages = uiState.totalPages
                    var scale by remember(currentPage) { mutableFloatStateOf(1f) }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Button(onClick = onNavigateUp) {
                                Text("뒤로")
                            }
                        }
                        Text(
                            text = uiState.fileName ?: "PDF Truth",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoadingPage) {
                                CircularProgressIndicator()
                            } else if (currentPageBitmap != null) {
                                Image(
                                    bitmap = currentPageBitmap.asImageBitmap(),
                                    contentDescription = "PDF 페이지 ${currentPage + 1}",
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .aspectRatio(0.707f)
                                        .scale(scale)
                                        .pointerInput(currentPage) {
                                            detectTapGestures(
                                                onDoubleTap = {
                                                    scale = if (scale <= 1f) 2f else 1f
                                                }
                                            )
                                        }
                                        .pinchToZoom(
                                            scale = scale,
                                            onScaleChange = { scale = it },
                                            minScale = 1f,
                                            maxScale = 5f,
                                        )
                                )
                            } else {
                                Text(uiState.errorMessage ?: "페이지를 불러오는 중입니다.")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = onPreviousPage,
                                enabled = currentPage > 0
                            ) {
                                Text("이전")
                            }
                            Text("페이지 ${currentPage + 1} / $totalPages")
                            Button(
                                onClick = onNextPage,
                                enabled = currentPage < totalPages - 1
                            ) {
                                Text("다음")
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                is ViewerUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("오류: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = onNavigateUp, modifier = Modifier.padding(top = 16.dp)) {
                            Text("뒤로")
                        }
                    }
                }
            }
        }
    }
}