package com.pdftruth.data.repository

import com.pdftruth.domain.model.PdfSearchOutcome
import com.pdftruth.domain.repository.PdfSearchRepository
import com.pdftruth.viewer.search.PdfSearchEngine

class PdfSearchRepositoryImpl(
    private val engine: PdfSearchEngine,
) : PdfSearchRepository {
    override suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): PdfSearchOutcome {
        return engine.search(documentUri, query, pageCount)
    }
}