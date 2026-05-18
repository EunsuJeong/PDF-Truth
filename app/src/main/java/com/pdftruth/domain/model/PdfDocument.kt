package com.pdftruth.domain.model

data class PdfDocument(
    val uri: String,
    val displayName: String,
    val fileSizeBytes: Long?,
)