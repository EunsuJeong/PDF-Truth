package com.pdftruth.data.repository

import com.pdftruth.data.local.PdfFileDataSource
import com.pdftruth.domain.model.PdfDocument
import com.pdftruth.domain.repository.DocumentRepository

class DocumentRepositoryImpl(
    private val pdfFileDataSource: PdfFileDataSource,
) : DocumentRepository {

    override suspend fun getDocument(uri: String): PdfDocument {
        return pdfFileDataSource.getDocument(uri)
    }
}