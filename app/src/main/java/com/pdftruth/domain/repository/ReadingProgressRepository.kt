package com.pdftruth.domain.repository

import com.pdftruth.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

interface ReadingProgressRepository {
    fun observeProgress(uri: String): Flow<ReadingProgress?>
    suspend fun saveProgress(progress: ReadingProgress)
}