package com.pdftruth.di

import android.content.Context
import androidx.room.Room
import com.pdftruth.data.local.AndroidPdfFileDataSource
import com.pdftruth.data.repository.BookmarkRepositoryImpl
import com.pdftruth.data.repository.DocumentRepositoryImpl
import com.pdftruth.data.repository.PdfSearchRepositoryImpl
import com.pdftruth.data.repository.ReaderPreferencesRepositoryImpl
import com.pdftruth.data.repository.ReadingProgressRepositoryImpl
import com.pdftruth.data.repository.RecentDocumentRepositoryImpl
import com.pdftruth.database.PdfTruthDatabase
import com.pdftruth.domain.repository.BookmarkRepository
import com.pdftruth.domain.repository.DocumentRepository
import com.pdftruth.domain.repository.PdfSearchRepository
import com.pdftruth.domain.repository.ReaderPreferencesRepository
import com.pdftruth.domain.repository.ReadingProgressRepository
import com.pdftruth.domain.repository.RecentDocumentRepository
import com.pdftruth.storage.ReaderPreferencesStorage
import com.pdftruth.viewer.search.PdfSearchEngineProvider

class AppContainer private constructor(context: Context) {

    private val appContext = context.applicationContext

    private val database: PdfTruthDatabase by lazy {
        Room.databaseBuilder(appContext, PdfTruthDatabase::class.java, "pdf_truth.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    private val readerPreferencesStorage: ReaderPreferencesStorage by lazy {
        ReaderPreferencesStorage(appContext)
    }

    private val searchEngineProvider: PdfSearchEngineProvider by lazy {
        PdfSearchEngineProvider(appContext)
    }

    val documentRepository: DocumentRepository by lazy {
        DocumentRepositoryImpl(AndroidPdfFileDataSource(appContext))
    }

    val recentDocumentRepository: RecentDocumentRepository by lazy {
        RecentDocumentRepositoryImpl(database.recentDocumentDao())
    }

    val readingProgressRepository: ReadingProgressRepository by lazy {
        ReadingProgressRepositoryImpl(database.readingProgressDao())
    }

    val readerPreferencesRepository: ReaderPreferencesRepository by lazy {
        ReaderPreferencesRepositoryImpl(readerPreferencesStorage)
    }

    val bookmarkRepository: BookmarkRepository by lazy {
        BookmarkRepositoryImpl(database.bookmarkDao())
    }

    val pdfSearchRepository: PdfSearchRepository by lazy {
        PdfSearchRepositoryImpl(searchEngineProvider.createDefault())
    }

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return instance ?: synchronized(this) {
                instance ?: AppContainer(context).also { instance = it }
            }
        }
    }
}
