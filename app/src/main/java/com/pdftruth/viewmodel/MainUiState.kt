package com.pdftruth.viewmodel

data class RecentDocumentItemUi(
    val uri: String,
    val displayName: String,
    val lastOpenedAt: Long,
    val lastPageIndex: Int?,
)

data class MainUiState(
    val title: String = "PDF Truth (True's)",
    val description: String = "Initial structure for the offline PDF reader MVP.",
    val canEnterViewer: Boolean = true,
    val recentDocuments: List<RecentDocumentItemUi> = emptyList(),
)