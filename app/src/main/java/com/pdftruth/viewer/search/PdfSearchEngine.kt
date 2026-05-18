package com.pdftruth.viewer.search

import com.pdftruth.domain.model.PdfSearchResult

interface PdfSearchEngine {
    suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): List<PdfSearchResult>
}