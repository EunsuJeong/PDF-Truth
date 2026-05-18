package com.pdftruth.domain.model

data class ReadingProgress(
    val uri: String,
    val lastPageIndex: Int,
    val updatedAt: Long,
)