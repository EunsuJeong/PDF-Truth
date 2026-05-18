package com.pdftruth.viewmodel

import android.graphics.Bitmap

sealed class ViewerUiState {
    object Idle : ViewerUiState()
    object Loading : ViewerUiState()
    data class Success(
        val bitmap: Bitmap,
        val pageCount: Int,
        val fileName: String? = null,
    ) : ViewerUiState()
    data class Error(val message: String) : ViewerUiState()
}