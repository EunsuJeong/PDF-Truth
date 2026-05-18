package com.pdftruth.domain.repository

import com.pdftruth.domain.model.PdfDocument

interface DocumentRepository {
    suspend fun getDocument(uri: String): PdfDocument
}