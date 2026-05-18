package com.pdftruth.viewer.search

import com.pdftruth.domain.model.PdfSearchResult

class PdfiumSearchEngine : PdfSearchEngine {
    override suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): List<PdfSearchResult> {
        // TODO: Wire PDFium text extraction/query pipeline in a dedicated implementation PR.
        // Placeholder only: do not present this as real text search yet.
        return emptyList()
    }
}
