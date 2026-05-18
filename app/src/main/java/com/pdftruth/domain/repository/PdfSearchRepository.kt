package com.pdftruth.domain.repository

import com.pdftruth.domain.model.PdfSearchOutcome

interface PdfSearchRepository {
    suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): PdfSearchOutcome
}