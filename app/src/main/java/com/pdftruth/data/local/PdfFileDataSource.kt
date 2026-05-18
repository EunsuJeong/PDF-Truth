package com.pdftruth.data.local

import com.pdftruth.domain.model.PdfDocument

interface PdfFileDataSource {
    suspend fun getDocument(uri: String): PdfDocument
}