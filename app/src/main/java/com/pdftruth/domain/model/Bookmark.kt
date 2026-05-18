package com.pdftruth.domain.model

data class Bookmark(
    val uri: String,
    val pageIndex: Int,
    val createdAt: Long,
)