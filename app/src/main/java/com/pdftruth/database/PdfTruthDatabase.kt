package com.pdftruth.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecentDocumentEntity::class, ReadingProgressEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PdfTruthDatabase : RoomDatabase() {
    abstract fun recentDocumentDao(): RecentDocumentDao
    abstract fun readingProgressDao(): ReadingProgressDao
}