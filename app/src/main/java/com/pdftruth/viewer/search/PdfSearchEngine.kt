package com.pdftruth.viewer.search

import com.pdftruth.domain.model.PdfSearchOutcome

interface PdfSearchEngine {
    suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): PdfSearchOutcome
}