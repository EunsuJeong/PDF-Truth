package com.pdftruth.viewmodel

import android.graphics.Bitmap

sealed class ViewerUiState {
    object Idle : ViewerUiState()
    object Loading : ViewerUiState()
    data class Success(
        val pageCount: Int,
        val pages: List<PageUiState>,
        val fileName: String? = null,
        val currentPage: Int = 0,
    ) : ViewerUiState()
    data class Error(val message: String) : ViewerUiState()
}

sealed class PageUiState {
    object Loading : PageUiState()
    data class BitmapReady(val bitmap: Bitmap) : PageUiState()
    data class Error(val message: String) : PageUiState()
}