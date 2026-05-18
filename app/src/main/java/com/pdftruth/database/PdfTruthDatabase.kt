package com.pdftruth.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecentDocumentEntity::class, ReadingProgressEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class PdfTruthDatabase : RoomDatabase() {
    abstract fun recentDocumentDao(): RecentDocumentDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookmarkDao(): BookmarkDao
}