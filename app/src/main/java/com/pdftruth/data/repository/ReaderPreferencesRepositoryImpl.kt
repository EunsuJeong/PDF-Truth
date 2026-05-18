package com.pdftruth.data.repository

import com.pdftruth.domain.repository.ReaderPreferencesRepository
import com.pdftruth.storage.ReaderPreferencesStorage
import kotlinx.coroutines.flow.Flow

class ReaderPreferencesRepositoryImpl(
    private val readerPreferencesStorage: ReaderPreferencesStorage,
) : ReaderPreferencesRepository {

    override fun observeLastOpenedDocumentUri(): Flow<String?> {
        return readerPreferencesStorage.observeLastOpenedDocumentUri()
    }

    override suspend fun saveLastOpenedDocumentUri(uri: String) {
        readerPreferencesStorage.saveLastOpenedDocumentUri(uri)
    }
}