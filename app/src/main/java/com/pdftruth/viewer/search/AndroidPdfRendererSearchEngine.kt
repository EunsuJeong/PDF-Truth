package com.pdftruth.viewer.search

import com.pdftruth.domain.model.PdfSearchResult

class AndroidPdfRendererSearchEngine : PdfSearchEngine {
    override suspend fun search(
        documentUri: String,
        query: String,
        pageCount: Int,
    ): List<PdfSearchResult> {
        // TODO: Android PdfRenderer does not provide text extraction APIs.
        // TODO: Real full-text search needs a different engine (e.g., PDFium/MuPDF) or OCR pipeline.
        return emptyList()
    }
}