package com.pdftruth.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val uri: String,
    val lastPageIndex: Int,
    val updatedAt: Long,
)