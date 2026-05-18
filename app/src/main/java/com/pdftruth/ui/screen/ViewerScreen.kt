package com.pdftruth.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pdftruth.viewmodel.ViewerUiState
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*

@Composable
fun ViewerScreen(
    uiState: ViewerUiState,
    onNavigateUp: () -> Unit,
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
                    val coroutineScope = rememberCoroutineScope()
                    // LazyListState와 ViewModel currentPage 동기화
                    LaunchedEffect(listState.firstVisibleItemIndex) {
                        if (listState.firstVisibleItemIndex != currentPage) {
                            // ViewModel에 현재 페이지 전달
                            // (무한 루프 방지: 실제 동기화 필요시만 호출)
                            onCurrentPageChanged?.invoke(listState.firstVisibleItemIndex)
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 상단 페이지 바
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
                            Text("페이지 ${currentPage + 1} / $pageCount", style = MaterialTheme.typography.bodyMedium)
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
                                            Image(
                                                bitmap = pageState.bitmap.asImageBitmap(),
                                                contentDescription = "PDF 페이지 ${idx + 1}",
                                                modifier = Modifier
                                                    .fillMaxWidth(0.9f)
                                                    .aspectRatio(0.707f)
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