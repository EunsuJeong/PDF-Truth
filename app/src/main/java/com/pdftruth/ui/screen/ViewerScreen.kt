package com.pdftruth.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pdftruth.viewmodel.ViewerUiState
import com.pdftruth.viewmodel.PageUiState
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import com.pdftruth.ui.gesture.pinchToZoom

@Composable
fun ViewerScreen(
    uiState: ViewerUiState,
    onNavigateUp: () -> Unit,
    onCurrentPageChanged: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchExecute: () -> Unit,
    onSearchResultClick: (Int) -> Unit,
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
                    val listState = rememberLazyListState()
                    val pageCount = uiState.pageCount
                    val pages = uiState.pages
                    val currentPage = uiState.currentPage
                    val isCurrentBookmarked = uiState.bookmarkedPages.contains(currentPage)
                    val coroutineScope = rememberCoroutineScope()
                    // 확대/축소 상태 (전체/페이지별 확장 가능)
                    var scale by remember { mutableStateOf(1f) }
                    val minScale = 1f
                    val maxScale = 5f
                    // LazyListState와 ViewModel currentPage 동기화
                    LaunchedEffect(listState.firstVisibleItemIndex) {
                        if (listState.firstVisibleItemIndex != currentPage) {
                            onCurrentPageChanged(listState.firstVisibleItemIndex)
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 상단 페이지/확대 바
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val prev = (currentPage - 1).coerceAtLeast(0)
                                        listState.animateScrollToItem(prev)
                                    }
                                },
                                enabled = currentPage > 0
                            ) { Text("이전") }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(uiState.fileName ?: "Unknown file", style = MaterialTheme.typography.labelLarge)
                                Text("페이지 ${currentPage + 1} / $pageCount", style = MaterialTheme.typography.bodyMedium)
                                Text("확대: ${(scale * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                                Text(uiState.documentUri ?: "", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val next = (currentPage + 1).coerceAtMost(pageCount - 1)
                                        listState.animateScrollToItem(next)
                                    }
                                },
                                enabled = currentPage < pageCount - 1
                            ) { Text("다음") }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChanged,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Search") },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = onSearchExecute) {
                                Text(if (uiState.isSearching) "..." else "Go")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = onToggleBookmark) {
                                Text(if (isCurrentBookmarked) "북마크 삭제" else "북마크 추가")
                            }
                        }

                        if (uiState.searchNotice != null) {
                            Text(
                                text = uiState.searchNotice,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            )
                        }

                        if (uiState.searchResults.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp)
                                    .padding(horizontal = 16.dp),
                            ) {
                                itemsIndexed(uiState.searchResults) { _, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("p${item.pageIndex + 1}: ${item.summary}")
                                        Button(onClick = {
                                            onSearchResultClick(item.pageIndex)
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(item.pageIndex)
                                            }
                                        }) {
                                            Text("Go")
                                        }
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 0.dp)
                        ) {
                            itemsIndexed(pages) { idx, pageState ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (pageState) {
                                        is PageUiState.Loading -> {
                                            CircularProgressIndicator()
                                        }
                                        is PageUiState.BitmapReady -> {
                                            // Pinch Zoom + scale 적용
                                            Image(
                                                bitmap = pageState.bitmap.asImageBitmap(),
                                                contentDescription = "PDF 페이지 ${idx + 1}",
                                                modifier = Modifier
                                                    .fillMaxWidth(0.9f)
                                                    .aspectRatio(0.707f)
                                                    .scale(scale)
                                                    .pointerInput(scale) {
                                                        detectTapGestures(
                                                            onDoubleTap = {
                                                                scale = if (scale <= 1f) 2f else 1f
                                                            }
                                                        )
                                                    }
                                                    .pinchToZoom(
                                                        scale = scale,
                                                        onScaleChange = { scale = it },
                                                        minScale = minScale,
                                                        maxScale = maxScale
                                                    )
                                            )
                                        }
                                        is PageUiState.Error -> {
                                            Text("페이지 ${idx + 1} 오류: ${pageState.message}", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                        Button(onClick = onNavigateUp, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Back")
                        }
                    }
                }
                is ViewerUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("오류: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = onNavigateUp, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }
}