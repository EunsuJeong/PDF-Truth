package com.pdftruth.data.repository

import com.pdftruth.database.ReadingProgressDao
import com.pdftruth.database.ReadingProgressEntity
import com.pdftruth.domain.model.ReadingProgress
import com.pdftruth.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingProgressRepositoryImpl(
    private val readingProgressDao: ReadingProgressDao,
) : ReadingProgressRepository {

    override fun observeProgress(uri: String): Flow<ReadingProgress?> {
        return readingProgressDao.observeProgress(uri).map { entity ->
            entity?.let {
                ReadingProgress(
                    uri = it.uri,
                    lastPageIndex = it.lastPageIndex,
                    updatedAt = it.updatedAt,
                )
            }
        }
    }

    override suspend fun saveProgress(progress: ReadingProgress) {
        readingProgressDao.upsert(
            ReadingProgressEntity(
                uri = progress.uri,
                lastPageIndex = progress.lastPageIndex,
                updatedAt = progress.updatedAt,
            )
        )
    }
}