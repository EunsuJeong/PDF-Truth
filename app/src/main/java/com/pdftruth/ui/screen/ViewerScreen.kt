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
                    // 현재 보이는 페이지 index 추적 (준비)
                    val visiblePage by remember {
                        derivedStateOf {
                            listState.firstVisibleItemIndex
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("페이지 ${visiblePage + 1} / $pageCount", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
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
                                                    .aspectRatio(0.707f) // A4 비율
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