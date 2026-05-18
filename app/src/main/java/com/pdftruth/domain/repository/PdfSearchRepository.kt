package com.pdftruth.domain.repository

import com.pdftruth.domain.model.PdfSearchResult

interface PdfSearchRepository {
    suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): List<PdfSearchResult>
}