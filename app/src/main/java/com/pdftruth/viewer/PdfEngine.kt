package com.pdftruth.viewer

interface PdfEngine {
    suspend fun openDocument(source: PdfDocumentSource): PdfDocumentSession
    suspend fun renderPage(session: PdfDocumentSession, request: PageRenderRequest): PageRenderResult
    suspend fun closeDocument(session: PdfDocumentSession)
}