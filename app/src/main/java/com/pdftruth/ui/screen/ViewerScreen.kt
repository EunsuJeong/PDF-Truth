package com.pdftruth.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pdftruth.ui.gesture.pinchToZoom
import com.pdftruth.viewmodel.PageUiState
import com.pdftruth.viewmodel.ViewerUiState
import kotlinx.coroutines.launch

@Composable
fun ViewerScreen(
    uiState: ViewerUiState,
    onNavigateUp: () -> Unit,
    onCurrentPageChanged: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchExecute: () -> Unit,
    onSearchResultClick: (Int) -> Unit,
    onToggleThumbnails: () -> Unit,
    onThumbnailClick: (Int) -> Unit,
    pdfUri: Uri? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (uiState) {
            is ViewerUiState.Idle -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
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

            is ViewerUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("오류: ", color = MaterialTheme.colorScheme.error)
                    Button(onClick = onNavigateUp, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Back")
                    }
                }
            }

            is ViewerUiState.Success -> {
                val listState = rememberLazyListState()
                val pageCount = uiState.pageCount
                val pages = uiState.pages
                val currentPage = uiState.currentPage
                val isCurrentBookmarked = uiState.bookmarkedPages.contains(currentPage)
                val coroutineScope = rememberCoroutineScope()
                var scale by remember { mutableStateOf(1f) }
                var showControls by rememberSaveable { mutableStateOf(false) }
                var showSearchPanel by rememberSaveable { mutableStateOf(false) }

                val minScale = 1f
                val maxScale = 5f

                LaunchedEffect(listState.firstVisibleItemIndex) {
                    if (listState.firstVisibleItemIndex != currentPage) {
                        onCurrentPageChanged(listState.firstVisibleItemIndex)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { showControls = !showControls })
                        },
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        itemsIndexed(pages) { idx, pageState ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                when (pageState) {
                                    is PageUiState.Loading -> CircularProgressIndicator()
                                    is PageUiState.Error -> {
                                        Text(
                                            text = "페이지  오류: ",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    is PageUiState.BitmapReady -> {
                                        Image(
                                            bitmap = pageState.bitmap.asImageBitmap(),
                                            contentDescription = "PDF 페이지 ",
                                            modifier = Modifier
                                                .fillMaxWidth(0.95f)
                                                .scale(scale)
                                                .pointerInput(scale) {
                                                    detectTapGestures(onDoubleTap = {
                                                        scale = if (scale <= 1f) 2f else 1f
                                                    })
                                                }
                                                .pinchToZoom(
                                                    scale = scale,
                                                    onScaleChange = { scale = it },
                                                    minScale = minScale,
                                                    maxScale = maxScale,
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showControls) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(10.dp),
                        ) {
                            Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 3.dp) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Button(onClick = onNavigateUp) { Text("Back") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.fileName ?: "PDF Viewer",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    item {
                                        Button(
                                            onClick = {
                                                val prev = (currentPage - 1).coerceAtLeast(0)
                                                coroutineScope.launch { listState.animateScrollToItem(prev) }
                                            },
                                            enabled = currentPage > 0,
                                        ) { Text("이전") }
                                    }
                                    item {
                                        Button(
                                            onClick = {
                                                val next = (currentPage + 1).coerceAtMost(pageCount - 1)
                                                coroutineScope.launch { listState.animateScrollToItem(next) }
                                            },
                                            enabled = currentPage < pageCount - 1,
                                        ) { Text("다음") }
                                    }
                                    item {
                                        Button(onClick = { showSearchPanel = !showSearchPanel }) {
                                            Text(if (showSearchPanel) "검색닫기" else "검색")
                                        }
                                    }
                                    item {
                                        Button(onClick = onToggleBookmark) {
                                            Text(if (isCurrentBookmarked) "북마크해제" else "북마크")
                                        }
                                    }
                                    item {
                                        Button(onClick = onToggleThumbnails) {
                                            Text(if (uiState.showThumbnails) "썸네일숨김" else "썸네일")
                                        }
                                    }
                                }
                            }

                            if (showSearchPanel) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.padding(top = 8.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            OutlinedTextField(
                                                value = uiState.searchQuery,
                                                onValueChange = onSearchQueryChanged,
                                                singleLine = true,
                                                label = { Text("Search") },
                                                modifier = Modifier.weight(1f),
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(onClick = onSearchExecute) {
                                                if (uiState.isSearching) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(18.dp),
                                                        strokeWidth = 2.dp,
                                                    )
                                                } else {
                                                    Text("Go")
                                                }
                                            }
                                        }

                                        if (uiState.searchNotice != null) {
                                            Text(
                                                text = uiState.searchNotice,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 6.dp),
                                            )
                                        }

                                        if (uiState.searchResults.isNotEmpty()) {
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 180.dp)
                                                    .padding(top = 6.dp),
                                            ) {
                                                itemsIndexed(uiState.searchResults) { _, item ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Text(
                                                            text = "p: ",
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Button(onClick = {
                                                            onSearchResultClick(item.pageIndex)
                                                            coroutineScope.launch {
                                                                listState.animateScrollToItem(item.pageIndex)
                                                            }
                                                        }) { Text("Go") }
                                                    }
                                                }
                                            }
                                        } else if (!uiState.isSearching && uiState.searchQuery.isNotBlank()) {
                                            Text(
                                                text = "검색 결과가 없습니다.",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 6.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.showThumbnails && showControls) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                            tonalElevation = 3.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            ) {
                                itemsIndexed(uiState.thumbnails, key = { _, item -> item.pageIndex }) { _, item ->
                                    val isSelected = item.pageIndex == currentPage
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .width(72.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 64.dp, height = 88.dp)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                    shape = RoundedCornerShape(6.dp),
                                                )
                                                .pointerInput(item.pageIndex) {
                                                    detectTapGestures(onTap = {
                                                        onThumbnailClick(item.pageIndex)
                                                        coroutineScope.launch {
                                                            listState.animateScrollToItem(item.pageIndex)
                                                        }
                                                    })
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            val bitmap = item.bitmap
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = "Thumbnail ",
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(2.dp),
                                                )
                                            } else {
                                                Text("...", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Text("", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (uiState.showThumbnails && showControls) 118.dp else 10.dp),
                    ) {
                        Text(
                            text = " / ",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
