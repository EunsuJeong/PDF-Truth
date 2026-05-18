package com.pdftruth.viewmodel

data class MainUiState(
    val title: String = "PDF Truth (True's)",
    val description: String = "Initial structure for the offline PDF reader MVP.",
    val canEnterViewer: Boolean = true,
)