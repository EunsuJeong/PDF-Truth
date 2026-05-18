package com.pdftruth.data.repository

import com.pdftruth.database.RecentDocumentDao
import com.pdftruth.database.RecentDocumentEntity
import com.pdftruth.domain.model.RecentDocument
import com.pdftruth.domain.repository.RecentDocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecentDocumentRepositoryImpl(
    private val recentDocumentDao: RecentDocumentDao,
) : RecentDocumentRepository {

    override fun observeRecentDocuments(): Flow<List<RecentDocument>> {
        return recentDocumentDao.observeRecentDocuments().map { entities ->
            entities.map { entity ->
                RecentDocument(
                    uri = entity.uri,
                    displayName = entity.displayName,
                    lastOpenedAt = entity.lastOpenedAt,
                )
            }
        }
    }

    override suspend fun saveRecentDocument(document: RecentDocument) {
        recentDocumentDao.upsert(
            RecentDocumentEntity(
                uri = document.uri,
                displayName = document.displayName,
                lastOpenedAt = document.lastOpenedAt,
            )
        )
    }
}