package com.pdftruth.viewmodel

import android.graphics.Bitmap

data class ThumbnailUi(
    val pageIndex: Int,
    val bitmap: Bitmap?,
)

data class SearchResultUi(
    val pageIndex: Int,
    val summary: String,
    val matchCount: Int = 0,
)

sealed class ViewerUiState {
    object Idle : ViewerUiState()
    object Loading : ViewerUiState()
    data class Success(
        val pageCount: Int,
        val pages: List<PageUiState>,
        val fileName: String? = null,
        val documentUri: String? = null,
        val currentPage: Int = 0,
        val bookmarkedPages: Set<Int> = emptySet(),
        val searchQuery: String = "",
        val searchResults: List<SearchResultUi> = emptyList(),
        val selectedSearchResultIndex: Int = -1,
        val isSearching: Boolean = false,
        val canCancelSearch: Boolean = false,
        val searchNotice: String? = null,
        val showThumbnails: Boolean = false,
        val thumbnails: List<ThumbnailUi> = emptyList(),
    ) : ViewerUiState()
    data class Error(val message: String) : ViewerUiState()
}

sealed class PageUiState {
    object Loading : PageUiState()
    data class BitmapReady(val bitmap: Bitmap) : PageUiState()
    data class Error(val message: String) : PageUiState()
}