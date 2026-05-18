package com.pdftruth.viewer.search

class PdfSearchEngineProvider {
    fun createDefault(): PdfSearchEngine {
        // Keep renderer fallback as default until PDFium implementation is production-ready.
        return AndroidPdfRendererSearchEngine()
    }

    fun createPdfiumPlaceholder(): PdfSearchEngine {
        return PdfiumSearchEngine()
    }
}
