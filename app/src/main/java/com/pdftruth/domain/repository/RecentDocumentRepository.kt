package com.pdftruth.domain.repository

import com.pdftruth.domain.model.RecentDocument
import kotlinx.coroutines.flow.Flow

interface RecentDocumentRepository {
    fun observeRecentDocuments(): Flow<List<RecentDocument>>
    suspend fun saveRecentDocument(document: RecentDocument)
}