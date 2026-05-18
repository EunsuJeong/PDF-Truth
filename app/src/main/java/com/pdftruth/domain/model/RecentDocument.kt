package com.pdftruth.domain.model

data class RecentDocument(
    val uri: String,
    val displayName: String,
    val lastOpenedAt: Long,
)