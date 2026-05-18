package com.pdftruth.domain.repository

import kotlinx.coroutines.flow.Flow

interface ReaderPreferencesRepository {
    fun observeLastOpenedDocumentUri(): Flow<String?>
    suspend fun saveLastOpenedDocumentUri(uri: String)
}