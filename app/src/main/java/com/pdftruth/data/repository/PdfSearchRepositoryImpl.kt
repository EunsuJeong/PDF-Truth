package com.pdftruth.data.repository

import com.pdftruth.domain.model.PdfSearchResult
import com.pdftruth.domain.repository.PdfSearchRepository
import com.pdftruth.viewer.search.PdfSearchEngine

class PdfSearchRepositoryImpl(
    private val engine: PdfSearchEngine,
) : PdfSearchRepository {
    override suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): List<PdfSearchResult> {
        return engine.search(documentUri, query, pageCount)
    }
}