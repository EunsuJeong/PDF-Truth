package com.pdftruth.viewer.search

import android.content.Context

class PdfSearchEngineProvider(private val appContext: Context) {
    fun createDefault(): PdfSearchEngine {
        return PdfiumSearchEngine(
            appContext = appContext,
            fallback = AndroidPdfRendererSearchEngine(),
        )
    }
}
